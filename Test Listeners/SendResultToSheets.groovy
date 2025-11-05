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

            Map payload = [
                token      : secret,
                suite      : currentSuiteId,
                runId      : "",   // ya lo genera Apps Script
                testCaseID : tcId,
                timeStamp  : new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                tester     : (GlobalVariable.hasProperty("Tester") ? GlobalVariable.Tester : ""),
                status     : status,
                durationMs : 0,
                build      : (GlobalVariable.hasProperty("Build") ? GlobalVariable.Build : ""),
                evidenceUrl: "",
                note       : ""
            ]

            // 👇 FORZAMOS a mandar bug si falló
            if (status == "FAIL") {
                payload["bug"] = [
                    id         : "AUTO-" + tcId,
                    title      : "Fallo en " + tcId,
                    severity   : "Media",
                    srt        : "",  // si no tienes pasos, lo dejas vacío
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
        KeywordUtil.logInfo("[Listener] Enviado a Google, código: " + code)
    }
}
