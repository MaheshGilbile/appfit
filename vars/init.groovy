// init.groovy

import metrics

// Automatically collect and insert metrics after every build
addBuildListener(new MetricsCollector())

class MetricsCollector extends hudson.model.BuildListener {
    @Override
    void completed(hudson.model.Build build, hudson.model.TaskListener listener) {
        def job = build.getProject()
        def metrics = metrics.collectMetrics(job, build)
        metrics.insertMetricsIntoDB(metrics)
    }
}