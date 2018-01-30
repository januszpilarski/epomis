package net.elenx.epomis.acceptor.pl.jobs

import com.google.api.client.http.HttpResponse
import net.elenx.epomis.acceptor.applicant.ApplicationForm
import net.elenx.epomis.connection.utils.FakeHttpResponseFactory
import net.elenx.epomis.entity.JobOffer
import net.elenx.epomis.service.connection6.response.ConnectionResponse
import net.elenx.epomis.service.connection6.response.HtmlResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spock.lang.Specification

class JobsApplicantTest extends Specification
{
    void "apply to jobs.pl"()
    {
        given:

        Map<String, String> cookie = new HashMap()
        cookie.put("sid", "12345")

        JobOffer jobOffer = JobOffer
            .builder()
            .href("https://www.jobs.pl/programista-2192288")
            .build()

        ApplicationForm<HtmlResponse> applicationForm = ApplicationForm
            .builder()
            .jobOffer(jobOffer)
            .status(ApplicationForm.Status.NEW)
            .baseDomain("jobs")
            .build()

        FakeHttpResponseFactory jobsHttpResponseFactory = new FakeHttpResponseFactory()

        HttpResponse httpResponseOk = jobsHttpResponseFactory.createHttpResponse(200, "OK")
        HttpResponse httpResponseFound = jobsHttpResponseFactory.createHttpResponse(302, "Found")

        InputStream inputStream = JobsApplicantTest.class.getResourceAsStream("Regular i Senior Developer - oferta pracy - JOBS.html")
        Document document = Jsoup.parse(inputStream,"UTF-8","https://jobs.pl/")

        ConnectionResponse connectionResponseOk = Mock()
        connectionResponseOk.getHttpResponse() >> httpResponseOk
        connectionResponseOk.getCookies() >> cookie

        ConnectionResponse connectionResponseFound = Mock()
        connectionResponseFound.getHttpResponse() >> httpResponseFound
        connectionResponseFound.getCookies() >> cookie

        HtmlResponse htmlResponseOk = HtmlResponse
            .builder()
            .connectionResponse(connectionResponseOk)
            .document(document)
            .build()

        HtmlResponse htmlResponseFound = HtmlResponse
            .builder()
            .connectionResponse(connectionResponseFound)
            .document(document)
            .build()

        JobsLogin jobsLogin = new JobsLogin()
        JobsDataFetcher jobsDataFetcher = new JobsDataFetcher()
        JobsFormDownloader jobsFormDownloader = new JobsFormDownloader()
        JobsApplicant jobsApplicant = new JobsApplicant()

        when:

        boolean isAppropriateForLogin = jobsLogin.isAppropriateFor(applicationForm)
        ApplicationForm applicationFormLogin = jobsLogin.advanceApplication(applicationForm, htmlResponseFound)
        boolean isAppropriateForDataFetcher = jobsDataFetcher.isAppropriateFor(applicationFormLogin)
        ApplicationForm applicationFormData = jobsDataFetcher.advanceApplication(applicationFormLogin, htmlResponseOk)
        boolean isAppropriateForFormDownloader = jobsFormDownloader.isAppropriateFor(applicationFormData)
        ApplicationForm applicationFormObtaining = jobsFormDownloader.advanceApplication(applicationFormData, htmlResponseFound)
        boolean isAppropriateForApplicant = jobsApplicant.isAppropriateFor(applicationFormObtaining)
        ApplicationForm applicationApplicant = jobsApplicant.advanceApplication(applicationFormObtaining, htmlResponseOk)

        then:

        isAppropriateForLogin
        applicationFormLogin.getStatus().toString() == "IN_PROGRESS"
        isAppropriateForDataFetcher
        applicationFormData.getStatus().toString() == "IN_PROGRESS"
        isAppropriateForFormDownloader
        applicationFormObtaining.getStatus().toString() == "IN_PROGRESS"
        isAppropriateForApplicant
        applicationApplicant.getStatus().toString() == "SUCCESS"
    }
}