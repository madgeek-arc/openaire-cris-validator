package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.config.AppConfig;
import org.eurocris.openaire.cris.validator.model.Job;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class CRISValidatorExecutorTest {

    private static final Logger logger = LogManager.getLogger(CRISValidatorExecutorTest.class);

    @Autowired
    CRISValidatorExecutor crisValidatorExecutor;

    @Value("#{${rule.weights}}")
    Map<String, Float> ruleWeights;

    @Test
    public void testRuleWeightsDefined() {
        assert this.ruleWeights.size() == 13;
        assert this.ruleWeights.get("check000_Identify") != null;
        assert this.ruleWeights.get("check010_MetadataFormats") != null;
        assert this.ruleWeights.get("check020_Sets") != null;
        assert this.ruleWeights.get("check100_CheckPublications") != null;
        assert this.ruleWeights.get("check200_CheckProducts") != null;
        assert this.ruleWeights.get("check300_CheckPatents") != null;
        assert this.ruleWeights.get("check400_CheckPersons") != null;
        assert this.ruleWeights.get("check500_CheckOrgUnits") != null;
        assert this.ruleWeights.get("check600_CheckProjects") != null;
        assert this.ruleWeights.get("check700_CheckFundings") != null;
        assert this.ruleWeights.get("check800_CheckEquipment") != null;
        assert this.ruleWeights.get("check900_CheckEvents") != null;
        assert this.ruleWeights.get("check990_CheckReferentialIntegrityAndFunctionalDependency") != null;
    }

    @Test
    public void test() {
        Job myJob = crisValidatorExecutor.submit("https://oamemtfa.uci.ru.nl/metis-oaipmh-endpoint/OAIHandler", "me");
        logger.info(crisValidatorExecutor.getStatus(myJob.getId()));
        assert myJob.getId() != null;
    }
}
