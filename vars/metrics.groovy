// metrics.groovy

import groovy.sql.Sql


def call(job, build) {
    def metrics = [:]
    // Collect application name
    metrics['application_name'] = job.name

    // Collect branch name
    metrics['branch_name'] = build.envVars['BRANCH_NAME']

    // Collect unit test status
	metrics['unit_test_coverage_status'] = job.getBuilds().last().getResult().toString() == 'SUCCESS' ? 1 : 0

    // Collect Sonar status
    //def sonarStatus = build.getAction(SonarAction.class)?.status
    metrics['sonar_status'] = job.getBuilds().last().getResult().toString() == 'SUCCESS' ? 1 : 0

    // Collect Artifactory upload status
    //def artifactoryUploadStatus = 
    metrics['artifactory_upload_status'] = job.getBuilds().last().getResult().toString() == 'SUCCESS' ? 1 : 0

    // Collect total success builds
    metrics['total_success_builds'] = job.builds.findAll { it.result == hudson.model.Result.SUCCESS }.size()

    // Collect total failed builds
    metrics['total_failed_builds'] = job.builds.findAll { it.result == hudson.model.Result.FAILURE }.size()

    // Calculate total success rate
    def totalBuilds = job.builds.size()
    metrics['total_success_rate'] = totalBuilds > 0? (metrics['total_success_builds'] / totalBuilds) * 100 : 0

    // Calculate average build time
    def buildTimes = job.builds.collect { it.duration }
    metrics['average_build_time'] = buildTimes.sum() / buildTimes.size()

    // Calculate success rate of build
    metrics['success_rate_of_build'] = metrics['total_success_rate']
    insertMetricsIntoDB(metrics)
    return metrics
}

def insertMetricsIntoDB(metrics) {
    def dbUrl = 'jdbc:postgresql://localhost:5432/postgres'
    def dbUser = 'postgres'
    def dbPassword = 'admin123'

    def sql = Sql.newInstance(dbUrl, dbUser, dbPassword, 'org.postgresql.Driver')

    sql.executeInsert('INSERT INTO AppFitMetrics (application_name, branch_name, unit_test_status, sonar_status, artifactory_upload_status, total_success_builds, total_failed_builds, total_success_rate, average_build_time, success_rate_of_build) VALUES (?,?,?,?,?,?,?,?,?,?)',
                      metrics['application_name'], metrics['branch_name'], metrics['unit_test_status'], metrics['sonar_status'], metrics['artifactory_upload_status'], metrics['total_success_builds'], metrics['total_failed_builds'], metrics['total_success_rate'], metrics['average_build_time'], metrics['success_rate_of_build'])
}