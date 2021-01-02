package de.neemann.digital.core.basic;

import de.neemann.digital.analyse.AnalyseException;
import de.neemann.digital.analyse.ModelAnalyser;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.core.BacktrackException;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Signal;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.testing.TestCaseElement;
import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnalyzerInsightTest extends TestCase {


    public static void main(String[] args) throws Exception {
        AnalyzerInsight.NameEntry[] list = new AnalyzerInsight.NameEntry[16];

        ElementLibrary lib = new ElementLibrary();
        ShapeFactory sf = new ShapeFactory(lib);
        File circuits = new File(Resources.getRoot(), "../../main/resources/insight/simple");
        for (File f : circuits.listFiles()) {
            Circuit c = Circuit.loadCircuit(f, sf);
            int index = createTable(lib, c);

            list[index] = new AnalyzerInsight.NameEntry(f.getName(), false);
            int center = (index >> 1) & 3;
            if (!(center == 0 || center == 3))
                list[index ^ 6] = new AnalyzerInsight.NameEntry(f.getName(), true);
        }

        for (int i = 0; i < 16; i++) {
            if (list[i] == null)
                System.out.println("null,");
            else
                System.out.println("new NameEntry(\"" + list[i].getName() + "\"," + list[i].getSwapInputs() + "),");
        }

    }

    private static int createTable(ElementLibrary lib, Circuit s) throws PinException, NodeException, ElementNotFoundException, AnalyseException, BacktrackException {

        assertEquals(0, s.getElements(v -> v.equalsDescription(TestCaseElement.DESCRIPTION)).size());

        Model model = new ModelCreator(s, lib).createModel(false);
        ModelAnalyser ma = new ModelAnalyser(model);

        ArrayList<Signal> inputs = ma.getInputs();
        assertEquals(2, inputs.size());
        assertEquals("in_1", inputs.get(0).getName());
        assertEquals("in_2", inputs.get(1).getName());
        ArrayList<Signal> outputs = model.getOutputs();
        assertEquals(1, outputs.size());
        assertEquals("Y", outputs.get(0).getName());
        TruthTable tt = ma.analyse();


        BoolTable r = tt.getResult(0);
        int index = 0;
        int mask = 1;
        for (int i = 0; i < 4; i++) {
            if (r.get(i).bool()) {
                index |= mask;
            }
            mask *= 2;
        }
        return index;
    }

}