package fr.nexus.hadoop;

import fr.nexus.hadoop.activity.ActivityPipeline;
import fr.nexus.hadoop.technical.TechnicalPipeline;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.Arrays;

public final class NexusDriver extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        if (args.length == 0) {
            usage();
            return 2;
        }
        String command = args[0];
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
        if ("activity".equals(command)) {
            return ActivityPipeline.run(getConf(), commandArgs);
        }
        if ("technical".equals(command)) {
            return TechnicalPipeline.run(getConf(), commandArgs);
        }
        usage();
        return 2;
    }

    private static void usage() {
        System.err.println("Usage:");
        System.err.println("  activity <telemetry-input> <players-input> <work-output> <dau-output>");
        System.err.println("  technical <telemetry-input> <logs-input> <work-output> <analytics-output>");
    }

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new NexusDriver(), args));
    }
}
