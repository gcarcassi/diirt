/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.sim;

import org.diirt.datasource.sim.Noise;
import org.diirt.datasource.sim.Ramp;
import org.diirt.datasource.sim.Sine;
import org.diirt.datasource.sim.GaussianNoise;
import org.diirt.datasource.sim.Replay;
import org.diirt.datasource.sim.NameParser;
import java.util.Arrays;
import java.util.List;
import org.diirt.vtype.VDouble;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.diirt.util.time.TimeDuration.*;
import org.diirt.util.time.Timestamp;

/**
 * Test simulated pv function names parsing
 *
 * @author carcassi
 */
public class NameParserTest {

    public NameParserTest() {
    }

    @Test
    public void testParameterParsing() {
        // A couple of correct combinations
        List<Object> parameters = NameParser.parseParameters("1.0,2.0");
        assertThat(parameters, equalTo(Arrays.asList((Object) 1.0, 2.0)));
        parameters = NameParser.parseParameters("-1,.5,  23.25");
        assertThat(parameters, equalTo(Arrays.asList((Object) (-1.0), 0.5,  23.25)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError1() {
        NameParser.parseParameters("1.0 2.0");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError2() {
        NameParser.parseParameters("1.O");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError3() {
        NameParser.parseParameters("1.1.2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError4() {
        NameParser.parseFunction("test(1.0 2.0)");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError5() {
        NameParser.parseFunction("test(1.O)");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError6() {
        NameParser.parseFunction("test(1.1.2)");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testError7() {
        NameParser.parseFunction("test1.2");
    }

    @Test
    public void testParsing() {
        // Couple of correct functions
        List<Object> parameters = NameParser.parseFunction("sine(1.0,2.0)");
        assertThat(parameters, equalTo(Arrays.asList((Object) "sine",  1.0, 2.0)));
        parameters = NameParser.parseFunction("ramp(-1,.5,  23.25)");
        assertThat(parameters, equalTo(Arrays.asList((Object) "ramp", -1.0, 0.5,  23.25)));
        parameters = NameParser.parseFunction("replay(\"test.xml\")");
        assertThat(parameters, equalTo(Arrays.asList((Object) "replay", "test.xml")));
    }

    @Test
    public void testRamp() {
        Ramp ramp = (Ramp) NameParser.createFunction("ramp(1.0, 10.0, 1.0, 1.0)");
        assertThat(ramp.nextValue().getValue(), equalTo(1.0));

    }

    @Test
    public void testSine() {
        Sine ramp = (Sine) NameParser.createFunction("sine(0.0, 10.0, 4.0, 1.0)");
        assertEquals(5.0, ramp.nextValue().getValue(), 0.0001);
        assertEquals(10.0, ramp.nextValue().getValue(), 0.0001);
        assertEquals(5.0, ramp.nextValue().getValue(), 0.0001);
        assertEquals(0.0, ramp.nextValue().getValue(), 0.0001);
    }

    @Test
    public void testNoise() {
        Noise noise1 = (Noise) NameParser.createFunction("noise(0.0, 10.0, 1.0)");
        Noise noise2 = (Noise) NameParser.createFunction("noise");
        // Forces use of variables
        assertThat(noise1.nextValue().getAlarmName(), notNullValue());
        assertThat(noise2.nextValue().getAlarmName(), notNullValue());
    }

    @Test
    public void gaussianNoise() {
        GaussianNoise noise1 = (GaussianNoise) NameParser.createFunction("gaussianNoise(0.0, 10.0, 1.0)");
        GaussianNoise noise2 = (GaussianNoise) NameParser.createFunction("gaussianNoise");
        GaussianNoise noise3 = (GaussianNoise) NameParser.createFunction("gaussianNoise()");
        // Forces use of variables
        assertThat(noise1.nextValue().getAlarmName(), notNullValue());
        assertThat(noise2.nextValue().getAlarmName(), notNullValue());
    }

    @Test
    public void replay() {
        Timestamp start = Timestamp.now();
        Replay replay = (Replay) NameParser.createFunction("replay(\"./src/test/resources/org/diirt/datasource/replay/parse1.xml\")");
        List<VDouble> values = replay.createValues(ofMillis(1000).after(start));
        assertThat(values.size(), equalTo(4));

        values = replay.createValues(ofMillis(100).after(start));
        assertThat(values.size(), equalTo(1));
    }

}