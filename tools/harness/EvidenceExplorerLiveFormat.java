import java.util.List;

/** JSON and static HTML for explicitly ephemeral local evidence state. */
final class EvidenceExplorerLiveFormat {
    private EvidenceExplorerLiveFormat() { }

    static String json(EvidenceExplorerLiveSnapshot value) {
        StringBuilder out = new StringBuilder("{\n  \"schema\":\"worldline.evidence-explorer-live.v1\",")
                .append("\n  \"verdict_authority\":false,\n  \"ephemeral_state_included\":true,")
                .append("\n  \"observed_at_epoch_ms\":").append(value.observedAtEpochMs()).append(',')
                .append("\n  \"canonical_snapshot_sha256\":")
                .append(EvidenceExplorerFormat.quote(value.canonicalSha256())).append(',')
                .append("\n  \"summary\":").append(summary(value)).append(',')
                .append("\n  \"execution\":").append(value.execution().json()).append(',')
                .append("\n  \"execution_links\":").append(executionLinks(value.executionLinks())).append(',')
                .append("\n  \"shared_cas\":").append(sharedCas(value.sharedCas())).append(',')
                .append("\n  \"admission\":").append(admission(value.admission())).append(',')
                .append("\n  \"queues\":[");
        append(out, value.queues().stream().map(EvidenceExplorerLiveFormat::queue).toList());
        out.append("],\n  \"cache_decisions\":[");
        append(out, value.cache().stream().map(EvidenceExplorerLiveFormat::cache).toList());
        return out.append("]\n}\n").toString();
    }

    static String html(EvidenceExplorerLiveSnapshot value) {
        StringBuilder out = new StringBuilder("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
                .append("<title>Worldline Live Evidence</title><style>")
                .append(EvidenceExplorerFormat.css()).append("</style></head><body><main>")
                .append("<header><p class=\"eyebrow\">Ephemeral local overlay</p><h1>Live Evidence</h1>")
                .append("<p>Observational state only; never a verdict source.</p></header><section class=\"cards\">")
                .append(card("Telemetry samples", value.execution().sampleCount()))
                .append(card("Failure samples", value.execution().failureCount()))
                .append(card("Native campaign links", value.executionLinks().linkedRuns()))
                .append(card("Failed native runs", value.executionLinks().failedNativeRuns()))
                .append(card("Orphan native runs", value.executionLinks().orphanNativeRuns()))
                .append(card("Shared CAS objects", shared(value, "objects")))
                .append(card("Shared CAS faults", shared(value, "faults")))
                .append(card("Queued tickets", value.queues().stream().mapToLong(
                        EvidenceExplorerLiveSnapshot.Queue::waiting).sum()))
                .append(card("Cache executions", count(value, "execute"))).append("</section>")
                .append("<section><h2>Native campaign evidence</h2><div class=\"table\"><table><thead><tr>")
                .append("<th>Request</th><th>Campaign</th><th>Shard</th><th>Backend</th>")
                .append("<th>Termination</th><th>Exit</th><th>Elapsed</th><th>Peak memory</th>")
                .append("</tr></thead><tbody>");
        for (EvidenceExplorerExecutionLinks.Link link : value.executionLinks().links()) out.append(linkRow(link));
        out.append("</tbody></table></div></section>")
                .append("<section><h2>Lease queues</h2><div class=\"table\"><table><thead><tr>")
                .append("<th>Queue</th><th>Waiting</th><th>Live</th><th>Abandoned</th>")
                .append("<th>Partial</th><th>Oldest wait</th></tr></thead><tbody>");
        for (EvidenceExplorerLiveSnapshot.Queue queue : value.queues()) out.append(queueRow(queue));
        out.append("</tbody></table></div></section><section><div class=\"bar\"><h2>Cache decisions</h2>")
                .append("<input id=\"filter\" placeholder=\"Filter smoke or decision…\"></div>")
                .append("<div class=\"table\"><table id=\"cache\"><thead><tr><th>Smoke</th>")
                .append("<th>Decision</th><th>Current tree</th><th>Reason</th></tr></thead><tbody>");
        for (EvidenceExplorerLiveSnapshot.CacheDecision item : value.cache()) out.append(cacheRow(item));
        out.append("</tbody></table></div></section><footer><a href=\"index.html\">Canonical explorer</a>")
                .append(" · <a href=\"live.json\">live.json</a> · canonical <code>")
                .append(shortHash(value.canonicalSha256())).append("</code></footer></main>")
                .append("<script>const f=document.querySelector('#filter'),")
                .append("r=[...document.querySelectorAll('#cache tbody tr')];")
                .append("f.oninput=()=>{const q=f.value.toLowerCase();")
                .append("r.forEach(x=>x.hidden=!x.innerText.toLowerCase().includes(q))}</script>")
                .append("</body></html>\n");
        return out.toString();
    }

    private static String summary(EvidenceExplorerLiveSnapshot value) {
        long queued = value.queues().stream().mapToLong(EvidenceExplorerLiveSnapshot.Queue::waiting).sum();
        return "{\"telemetry_samples\":" + value.execution().sampleCount()
                + ",\"failure_samples\":" + value.execution().failureCount()
                + ",\"native_runs\":" + value.executionLinks().nativeRuns()
                + ",\"linked_native_runs\":" + value.executionLinks().linkedRuns()
                + ",\"failed_native_runs\":" + value.executionLinks().failedNativeRuns()
                + ",\"orphan_native_runs\":" + value.executionLinks().orphanNativeRuns()
                + ",\"partial_native_runs\":" + value.executionLinks().partialNativeRuns()
                + ",\"dangling_campaign_bindings\":" + value.executionLinks().danglingCampaignBindings()
                + ",\"referenced_logs\":" + value.executionLinks().referencedLogs()
                + ",\"orphan_logs\":" + value.executionLinks().orphanLogs()
                + ",\"missing_logs\":" + value.executionLinks().missingLogs()
                + ",\"shared_cas_objects\":" + shared(value, "objects")
                + ",\"shared_cas_faults\":" + shared(value, "faults")
                + ",\"queued_tickets\":" + queued + ",\"cache_decisions\":"
                + value.cache().size() + ",\"cache_execute\":" + count(value, "execute") + "}";
    }
    private static long count(EvidenceExplorerLiveSnapshot value, String decision) {
        return value.cache().stream().filter(item -> decision.equals(item.decision())).count();
    }
    private static long shared(EvidenceExplorerLiveSnapshot value, String field) {
        SharedCasInventory.Snapshot shared = value.sharedCas(); if (shared == null) return 0L;
        return "objects".equals(field) ? shared.totalObjects() : shared.corruptObjects()
                + shared.malformedEntries() + shared.missingSignedReferences();
    }
    private static String sharedCas(SharedCasInventory.Snapshot value) {
        return value == null ? "null" : SharedCasInventory.json(value).strip();
    }
    private static String admission(EvidenceExplorerLiveSnapshot.Admission value) {
        if (value == null) return "null";
        return "{\"direction\":\"lower-only\",\"os\":" + quote(value.os())
                + ",\"architecture\":" + quote(value.architecture()) + ",\"sample_count\":"
                + value.sampleCount() + ",\"observed_peak_memory_bytes\":"
                + value.observedPeakMemoryBytes() + ",\"minimum_worker_memory_bytes\":"
                + value.minimumWorkerMemoryBytes() + "}";
    }
    private static String executionLinks(EvidenceExplorerExecutionLinks.Snapshot value) {
        StringBuilder out = new StringBuilder("{\"native_runs\":").append(value.nativeRuns())
                .append(",\"linked_runs\":").append(value.linkedRuns())
                .append(",\"failed_runs\":").append(value.failedNativeRuns())
                .append(",\"orphan_native_runs\":").append(value.orphanNativeRuns())
                .append(",\"partial_native_runs\":").append(value.partialNativeRuns())
                .append(",\"campaign_bindings\":").append(value.campaignBindings())
                .append(",\"dangling_campaign_bindings\":").append(value.danglingCampaignBindings())
                .append(",\"referenced_logs\":").append(value.referencedLogs())
                .append(",\"orphan_logs\":").append(value.orphanLogs())
                .append(",\"missing_logs\":").append(value.missingLogs()).append(",\"links\":[");
        append(out, value.links().stream().map(EvidenceExplorerLiveFormat::link).toList());
        return out.append("]}").toString();
    }
    private static String link(EvidenceExplorerExecutionLinks.Link value) {
        return "{\"request_sha256\":" + quote(value.requestSha256()) + ",\"campaign_id\":"
                + EvidenceExplorerFormat.nullable(value.campaignId()) + ",\"campaign_plan_sha256\":"
                + EvidenceExplorerFormat.nullable(value.campaignPlanSha256()) + ",\"shard_index\":"
                + (value.shardIndex() == null ? "null" : value.shardIndex()) + ",\"backend\":"
                + quote(value.backend()) + ",\"executor_version\":" + quote(value.executorVersion())
                + ",\"termination\":" + quote(value.termination()) + ",\"exit_code\":"
                + number(value.exitCode()) + ",\"elapsed_ms\":" + value.elapsedMs()
                + ",\"peak_memory_bytes\":" + number(value.peakMemoryBytes())
                + ",\"peak_processes\":" + number(value.peakProcesses()) + "}";
    }
    private static String queue(EvidenceExplorerLiveSnapshot.Queue value) {
        return "{\"label\":" + quote(value.label()) + ",\"waiting\":" + value.waiting()
                + ",\"live\":" + value.live() + ",\"abandoned\":" + value.abandoned()
                + ",\"partial\":" + value.partial() + ",\"oldest_wait_ms\":"
                + (value.oldestWaitMs() == null ? "null" : value.oldestWaitMs()) + "}";
    }
    private static String cache(EvidenceExplorerLiveSnapshot.CacheDecision value) {
        return "{\"smoke\":" + quote(value.smoke()) + ",\"fingerprint\":"
                + quote(value.fingerprint()) + ",\"cache_enabled\":" + value.cacheEnabled()
                + ",\"local_proof\":" + value.localProof() + ",\"tracked_pin\":"
                + value.trackedPin() + ",\"decision\":" + quote(value.decision())
                + ",\"reason\":" + quote(value.reason()) + ",\"current_tree_mode\":"
                + EvidenceExplorerFormat.nullable(value.currentTreeMode()) + "}";
    }
    private static String queueRow(EvidenceExplorerLiveSnapshot.Queue value) {
        return "<tr><td>" + html(value.label()) + "</td><td>" + value.waiting()
                + "</td><td>" + value.live() + "</td><td>" + value.abandoned() + "</td><td>"
                + value.partial() + "</td><td>" + duration(value.oldestWaitMs()) + "</td></tr>";
    }
    private static String cacheRow(EvidenceExplorerLiveSnapshot.CacheDecision value) {
        return "<tr><td><strong>" + html(value.smoke()) + "</strong></td><td><span class=\"tag\">"
                + html(value.decision()) + "</span></td><td>"
                + html(value.currentTreeMode() == null ? "—" : value.currentTreeMode())
                + "</td><td>" + html(value.reason()) + "</td></tr>";
    }
    private static String linkRow(EvidenceExplorerExecutionLinks.Link value) {
        return "<tr><td><code>" + shortHash(value.requestSha256()) + "</code></td><td>"
                + html(value.campaignId() == null ? "orphan" : value.campaignId()) + "</td><td>"
                + (value.shardIndex() == null ? "—" : value.shardIndex()) + "</td><td>"
                + html(value.backend()) + "</td><td>" + html(value.termination()) + "</td><td>"
                + numberText(value.exitCode()) + "</td><td>" + value.elapsedMs()
                + " ms</td><td>" + numberText(value.peakMemoryBytes()) + "</td></tr>";
    }
    private static String card(String label, long value) {
        return "<article><strong>" + value + "</strong><span>" + html(label) + "</span></article>";
    }
    private static String duration(Long millis) { return millis == null ? "—" : millis + " ms"; }
    private static String number(Long value) { return value == null ? "null" : value.toString(); }
    private static String numberText(Long value) { return value == null ? "—" : value.toString(); }
    private static String shortHash(String value) { return html(value.substring(0, 12)); }
    private static String quote(String value) { return EvidenceExplorerFormat.quote(value); }
    private static String html(String value) { return EvidenceExplorerFormat.html(value); }
    private static void append(StringBuilder out, List<String> values) {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) out.append(','); out.append("\n    ").append(values.get(index));
        }
        if (!values.isEmpty()) out.append('\n').append("  ");
    }
}
