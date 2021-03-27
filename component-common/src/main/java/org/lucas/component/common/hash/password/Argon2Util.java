package org.lucas.component.common.hash.password;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public abstract class Argon2Util {

    private static final Argon2 PWD_PROVIDER = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    public static String hash(String pwd) {
        var _p = pwd.toCharArray();
        try {
            return PWD_PROVIDER.hash(3, 1024, 3, _p);
        } finally {
            PWD_PROVIDER.wipeArray(_p);
        }
    }

    public static boolean verify(String pwd, String hashPwd) {
        var _p = pwd.toCharArray();
        try {
            return PWD_PROVIDER.verify(hashPwd, _p);
        } finally {
            PWD_PROVIDER.wipeArray(_p);
        }
    }

}
