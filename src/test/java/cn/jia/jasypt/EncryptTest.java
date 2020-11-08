package cn.jia.jasypt;

import cn.jia.BaseTest;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EncryptTest extends BaseTest {
    @Autowired
    StringEncryptor stringEncryptor;

    @Test
    void encrypt() {
        String mysqlusername = stringEncryptor.encrypt("root");
        System.out.println("mysqlusername:"+mysqlusername);
    }

    @Test
    void decrypt() {
        String decrypt = stringEncryptor.decrypt("tKvW1SPWETa1O49yl2IktWaaNxC+vZX1SvjEZ+Myd7LcMFFK1UQalqVuIx7G6nx+");
        System.out.println("decrypt:" + decrypt);
    }
}
