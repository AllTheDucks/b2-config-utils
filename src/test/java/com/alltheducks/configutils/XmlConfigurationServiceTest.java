package com.alltheducks.configutils;

import static org.junit.Assert.*;

import com.alltheducks.configutils.service.XmlConfigurationService;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
* Created by wiley on 19/11/14.
*/
public class XmlConfigurationServiceTest {

    XmlConfigurationService<MyConfig> configService;

    @Before
    public void setup() {
        File testDir = new File("build/tests");
        if (!testDir.exists()) {
            testDir.mkdirs();
        } else {
            new File("build/tests/myconfig.xml").delete();
        }
        configService = new XmlConfigurationService<MyConfig>();

        // Work around for inner classes.
        configService.getXStream().omitField(MyConfig.class, "this$0");
    }

    @Test
    public void loadConfig_withoutExistingFile_returnsNull() {
        configService.setConfigurationXMLFile(new File("build/tests/myconfig.xml"));

        MyConfig config = configService.loadConfiguration();

        assertNull(config);
    }

    @Test
    public void persistConfig_withValidObject_createsFile() {
        configService.setConfigurationXMLFile(new File("build/tests/myconfig.xml"));

        MyConfig config = new MyConfig();
        config.setSettingOne("My Value One");
        config.setSettingTwo("My Value Two");

        configService.persistConfiguration(config);

        assertTrue(new File("build/tests/myconfig.xml").exists());
    }


    public class MyConfig {
        String settingOne;
        String settingTwo;

        public String getSettingOne() {
            return settingOne;
        }

        public void setSettingOne(String settingOne) {
            this.settingOne = settingOne;
        }

        public String getSettingTwo() {
            return settingTwo;
        }

        public void setSettingTwo(String settingTwo) {
            this.settingTwo = settingTwo;
        }
    }
}
