package org.rhq.enterprise.server.storage.maintenance;

/**
 * A {@link StorageMaintenanceJob maintenance job} can have an associated configuration, and each step in the job can
 * also have an associated configuration. That configuration object allows us to store arbitrary data like resource
 * operation parameters or job-level parameters. There are a number of known, expected configuration properties. This
 * class provides constants for those property names along with a description of them.
 *
 * @author John Sanda
 */
public final class JobProperties {

    /**
     * Most jobs will include a <code>target</code> that specifies the storage node (or other resource) for which
     * maintenance is to be performed. The value of the <code>target</code> property will depend on on the job.
     */
    public static final String TARGET = "target";

    /**
     * A job-level property that is used to determine whether or not job steps need to be recalculated.
     */
    public static final String CLUSTER_SNAPSHOT = "clusterSnapshot";

    /**
     * Typically used to specify parameters for a resource operation.
     */
    public static final String PARAMETERS = "parameters";

    /**
     * Specifies the {@link org.rhq.enterprise.server.storage.maintenance.StepFailureStrategy failure strategy} for
     * a step. Each {@link org.rhq.enterprise.server.storage.maintenance.MaintenanceStepRunner#getFailureStrategy() step runner}
     * defines a failure strategy. This property can be used as a hint or as an override. It should be a string whose
     * value is one of the {@link org.rhq.enterprise.server.storage.maintenance.StepFailureStrategy failure strategies}
     */
    public static final String FAILURE_STRATEGY = "failureStrategy";

    /**
     * Specifies an {@link org.rhq.core.domain.cloud.StorageNode.OperationMode operation mode} as a string
     */
    public static final String OPERATION_MODE = "operationMode";

    private JobProperties() {}

}
