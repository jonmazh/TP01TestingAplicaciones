package internal

import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.main.TestCaseMain


/**
 * This class is generated automatically by Katalon Studio and should not be modified or deleted.
 */
public class GlobalVariable {
     
    /**
     * <p></p>
     */
    public static Object WebhookUrl
     
    /**
     * <p></p>
     */
    public static Object WebhookSecret
     
    /**
     * <p></p>
     */
    public static Object Build
     
    /**
     * <p></p>
     */
    public static Object Tester
     

    static {
        try {
            def selectedVariables = TestCaseMain.getGlobalVariables("default")
			selectedVariables += TestCaseMain.getGlobalVariables(RunConfiguration.getExecutionProfile())
    
            WebhookUrl = selectedVariables['WebhookUrl']
            WebhookSecret = selectedVariables['WebhookSecret']
            Build = selectedVariables['Build']
            Tester = selectedVariables['Tester']
            
        } catch (Exception e) {
            TestCaseMain.logGlobalVariableError(e)
        }
    }
}
