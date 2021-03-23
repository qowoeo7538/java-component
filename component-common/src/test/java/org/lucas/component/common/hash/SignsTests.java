package org.lucas.component.common.hash;

import com.google.common.base.Charsets;
import org.junit.jupiter.api.Test;

class SignsTests {

    @Test
    void signsTest(){
        System.out.println(Signs.use()
                .algorithm(Signs.HashAlgorithm.MD5)
                .withPayload("1", Charsets.UTF_8)
                .displayAs(Signs.Display.HEX)
                .signature());
    }

}
