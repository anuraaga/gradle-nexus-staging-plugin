package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import io.codearte.gradle.nexus.FunctionalTestHelperTrait
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.infra.WrongNumberOfStagingProfiles

class StagingProfileFetcherSpec extends BaseOperationExecutorSpec implements FunctionalTestHelperTrait {

    private static final String GET_STAGING_PROFILES_PATH = "/staging/profiles"
    private static final String GET_STAGING_PROFILES_FULL_URL = MOCK_SERVER_HOST + GET_STAGING_PROFILES_PATH

    def "should get staging profile id from server"() {
        given:
            def client = Mock(SimplifiedHttpJsonRestClient)
            client.get(GET_STAGING_PROFILES_FULL_URL) >> {
                new JsonSlurper().parse(this.getClass().getResource("2stagingProfilesShrunkResponse.json"))
            }
            StagingProfileFetcher fetcher = new StagingProfileFetcher(client, MOCK_SERVER_HOST)
        when:
            String stagingProfileId = fetcher.getStagingProfileIdForPackageGroup("io.codearte")
        then:
            println stagingProfileId
            stagingProfileId == TEST_STAGING_PROFILE_ID
    }

    def "should throw meaningful exception for not matching profiles for package group"() {
        given:
            def client = Mock(SimplifiedHttpJsonRestClient)
            client.get(GET_STAGING_PROFILES_FULL_URL) >> {
                [data: [[id: 1, name: "other1"], [id: 2, name: "other2"]]]
            }
            StagingProfileFetcher fetcher = new StagingProfileFetcher(client, MOCK_SERVER_HOST)
        when:
            fetcher.getStagingProfileIdForPackageGroup("wrongGroup")
        then:
            def e = thrown(WrongNumberOfStagingProfiles)
            e.packageGroup == "wrongGroup"
            e.numberOfProfiles == 0
    }

    def "should throw meaningful exception for too many matching profiles for package group"() {
        given:
            def client = Mock(SimplifiedHttpJsonRestClient)
            client.get(GET_STAGING_PROFILES_FULL_URL) >> {
                [data: [[id: 1, name: "tooMuch"], [id: 2, name: "tooMuch"]]]
            }
            StagingProfileFetcher fetcher = new StagingProfileFetcher(client, MOCK_SERVER_HOST)
        when:
            fetcher.getStagingProfileIdForPackageGroup("tooMuch")
        then:
            def e = thrown(WrongNumberOfStagingProfiles)
            e.packageGroup == "tooMuch"
            e.numberOfProfiles == 2
    }
}
