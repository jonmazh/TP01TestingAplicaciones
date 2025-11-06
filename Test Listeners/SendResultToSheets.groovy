import com.kms.katalon.core.annotation.BeforeTestSuite
import com.kms.katalon.core.annotation.AfterTestCase
import com.kms.katalon.core.context.TestSuiteContext
import com.kms.katalon.core.context.TestCaseContext
import com.kms.katalon.core.util.KeywordUtil
import internal.GlobalVariable
import groovy.json.JsonOutput

class SendResultToSheets {

    private String currentSuiteId = ""

    @BeforeTestSuite
    def beforeSuite(TestSuiteContext testSuiteContext) {
        currentSuiteId = testSuiteContext.getTestSuiteId()
    }

    @AfterTestCase
	def afterTestCase(TestCaseContext testCaseContext) {
    	try {
	        String webhookUrl = GlobalVariable.WebhookUrl
	        String secret     = GlobalVariable.WebhookSecret
	
	        String fullId  = testCaseContext.getTestCaseId()
	        String tcId    = fullId.tokenize("/").last()
	        String kStatus = testCaseContext.getTestCaseStatus()     // PASSED / FAILED / ERROR
	        String status  = (kStatus == "PASSED") ? "PASS" : "FAIL"
	
	        // 👇 forzamos a leer la variable global SIN truquitos
	        String tester = ""
	        try {
	            tester = GlobalVariable.Tester   // <-- pon aquí el nombre EXACTO que tienes en el perfil
	        } catch (Exception ex) {
	            tester = ""
	        }
	
	        Map payload = [
	            token      : secret,
	            suite      : currentSuiteId,
	            runId      : "",
	            testCaseID : tcId,
	            timeStamp  : new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'"),
	            tester     : tester,
	            status     : status,
	            durationMs : 0,
	            build      : (GlobalVariable.hasProperty("Build") ? GlobalVariable.Build : ""),
	            evidenceUrl: "",
	            note       : ""
	        ]
	
	        if (status == "FAIL") {
	            payload["bug"] = [
	                id         : "", // dejamos que Apps Script genere B-00X
	                title      : "Fallo en " + tcId,
	                severity   : "Media",
	                srt        : "",
	                evidenceUrl: "",
	                notes      : testCaseContext.getMessage()
	            ]
	        }
	
	        sendPost(webhookUrl, payload)
	
	    } catch (Exception e) {
	        KeywordUtil.logInfo("No se pudo enviar el resultado al sheet: " + e.message)
	    }
	}


    private void sendPost(String url, Map payload) {
    URL endpoint = new URL(url)
    HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection()
    conn.setRequestMethod("POST")
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setDoOutput(true)

    String json = JsonOutput.toJson(payload)
    conn.getOutputStream().write(json.getBytes("UTF-8"))
    conn.getOutputStream().flush()
    conn.getOutputStream().close()

    int code = conn.getResponseCode()

    // 👇 leer la respuesta del Apps Script
    InputStream is
    if (code >= 200 && code < 400) {
        is = conn.getInputStream()
    } else {
        is = conn.getErrorStream()
    }
    String resp = is != null ? is.getText("UTF-8") : ""
    com.kms.katalon.core.util.KeywordUtil.logInfo("[Listener] Google respondió: " + code + " - " + resp)
	KeywordUtil.logInfo("[Listener] Payload tester = " + tester)
}

}
