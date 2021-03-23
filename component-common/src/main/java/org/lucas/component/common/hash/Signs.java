package org.lucas.component.common.hash;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.netty.buffer.ByteBufUtil;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.API.Option;
import static io.vavr.API.Tuple;

public class Signs {

    public static final Joiner.MapJoiner emptyStyle = Joiner.on("").withKeyValueSeparator("");

    private static final IllegalArgumentException MISS_SIGNATURE_ALGORITHM = new IllegalArgumentException("You should use at least one signature algorithm.");

    public static Signs.Signer use() {
        return new Signs.Signer();
    }

    private Signs() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }


    @SuppressWarnings({"deprecation", "UnstableApiUsage"})
    public enum HashAlgorithm implements Function<byte[], HashCode> {
        MD5(Hashing.md5()),
        SHA1(Hashing.sha1()),
        SHA256(Hashing.sha256()),
        SHA512(Hashing.sha512());

        private final HashFunction hashFunc;

        HashAlgorithm(HashFunction hashFunc) {
            this.hashFunc = hashFunc;
        }

        @Override
        public HashCode apply(byte[] payload) {
            return this.getHashFunc().newHasher().putBytes(payload).hash();
        }

        public HashFunction getHashFunc() {
            return hashFunc;
        }
    }

    @SuppressWarnings({"UnstableApiUsage"})
    public enum Display implements Function<HashCode, String> {
        HEX(ByteBufUtil::hexDump),
        BASE64(Base64.getEncoder()::encodeToString),
        BASE64_URL(Base64.getUrlEncoder()::encodeToString),
        ;

        private final Function<byte[], String> strFunc;

        Display(Function<byte[], String> strFunc) {
            this.strFunc = strFunc;
        }

        @Override
        public String apply(HashCode hashCode) {
            return this.getStrFunc().apply(hashCode.asBytes());
        }


        public Function<byte[], String> getStrFunc() {
            return strFunc;
        }
    }

    @SuppressWarnings({"UnstableApiUsage"})
    public enum HmacAlgorithm implements BiFunction<byte[], byte[], HashCode> {
        HMAC_MD5(Hashing::hmacMd5),
        HMAC_SHA1(Hashing::hmacSha1),
        HMAC_SHA256(Hashing::hmacSha256),
        HMAC_SHA512(Hashing::hmacSha512),
        ;

        private final Function<byte[], HashFunction> hashFunc;

        HmacAlgorithm(Function<byte[], HashFunction> hashFunc) {
            this.hashFunc = hashFunc;
        }

        @Override
        public HashCode apply(byte[] keys, byte[] payload) {
            return this.getHashFunc().apply(keys).newHasher().putBytes(payload).hash();
        }

        public Function<byte[], HashFunction> getHashFunc() {
            return hashFunc;
        }
    }

    public static class Signer {

        private HashAlgorithm hash;
        private HmacAlgorithm hmac;
        private Display display;
        private byte[] key;
        private byte[] payload;

        /**
         * signature use the specified {@code HashAlgorithm}
         *
         * @param hash the HashAlgorithm
         * @return Signer instance itself for chaining
         */
        public Signer algorithm(@NonNull HashAlgorithm hash) {
            Preconditions.checkArgument(this.hmac == null, "Sing should use one algorithm");
            this.hash = Preconditions.checkNotNull(hash);
            return this;
        }

        /**
         * signature use the specified {@code HmacAlgorithm}
         *
         * @param hmac the HmacAlgorithm
         * @return Signer instance itself for chaining
         */
        public Signer algorithm(@NonNull HmacAlgorithm hmac) {
            Preconditions.checkArgument(this.hash == null, "Sing should use one algorithm");
            this.hmac = Preconditions.checkNotNull(hmac);
            return this;
        }

        /**
         * the payload to signature
         *
         * @param payload payload
         * @return Signer instance itself for chaining
         */
        public Signer withPayload(byte[] payload) {
            Preconditions.checkArgument(payload.length > 0);
            this.payload = payload;
            return this;
        }

        /**
         * the payload with the {@code Charset} to signature
         *
         * @param payload payload
         * @param charset Charset
         * @return Signer instance itself for chaining
         */
        public Signer withPayload(@NonNull String payload, @NonNull Charset charset) {
            Preconditions.checkArgument(StringUtils.isNoneBlank(payload));
            return withPayload(payload.getBytes(charset));
        }

        /**
         * the payload with the {@link StandardCharsets#UTF_8} to signature
         *
         * @param payload payload
         * @return Signer instance itself for chaining
         */
        public Signer withUtf8Payload(@NonNull String payload) {
            return withPayload(payload, StandardCharsets.UTF_8);
        }

        /**
         * the map use empty style
         *
         * <pre>{@code
         * Map<String, String> payload = ImmutableMap.of("he","ll");
         * // the payload will be hell
         * // the secretKey be o
         * // the final payload be hello
         * }</pre>
         *
         * @param payload   the map payload
         * @param secretKey the secretKey
         * @return Signer instance itself for chaining
         */
        public Signer withMapPayloadAndEmptyStyleKeyLast(
                Map<String, String> payload, String secretKey) {
            String req = emptyStyle.join(payload);
            return withUtf8Payload(req + secretKey);
        }

        /**
         * setting the hmac key
         *
         * @param key key
         * @return Signer instance itself for chaining
         */
        public Signer withKey(byte[] key) {
            Preconditions.checkArgument(key.length > 0);
            this.key = key;
            return this;
        }

        /**
         * setting the hmac key
         *
         * @param key key
         * @return Signer instance itself for chaining
         */
        public Signer withKey(@NonNull String key, @NonNull Charset charset) {
            Preconditions.checkArgument(StringUtils.isNoneBlank(key));
            return withKey(key.getBytes(charset));
        }

        /**
         * signature displayed as what
         *
         * @param display Display
         * @return Signer instance itself for chaining
         */
        public Signer displayAs(Display display) {
            this.display = Preconditions.checkNotNull(display);
            return this;
        }

        /**
         * signature the payload with the {@link Signer#hash} or {@link Signer#hmac} and display use
         * {@link Signer#display}
         *
         * @return signature
         */
        public @NonNull String signature() {
            Preconditions.checkNotNull(payload);
            Preconditions.checkNotNull(display);
            Tuple2<Option<HashAlgorithm>, Option<HmacAlgorithm>> algorithms = Tuple(Option(hash), Option(hmac));
            return Match(algorithms)
                    .of(
                            Case(
                                    $(t -> t._1().isDefined()),
                                    opt -> {
                                        HashAlgorithm algorithm = opt._1.get();
                                        return algorithm.andThen(display).apply(payload);
                                    }),
                            Case(
                                    $(t -> t._2().isDefined()),
                                    opt -> {
                                        Preconditions.checkNotNull(key);
                                        HmacAlgorithm algorithm = opt._2.get();
                                        return algorithm.andThen(display).apply(key, payload);
                                    }),
                            Case(
                                    $(),
                                    opt -> {
                                        throw MISS_SIGNATURE_ALGORITHM;
                                    }));
        }
    }
}
