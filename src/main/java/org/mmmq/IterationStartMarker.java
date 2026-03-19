package org.mmmq;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;

@StackTrace(false)
@Category("JMH Benchmark")
@Label("Iteration Start Marker")
public class IterationStartMarker extends Event {

    @Label("Message")
    public String message;
}
