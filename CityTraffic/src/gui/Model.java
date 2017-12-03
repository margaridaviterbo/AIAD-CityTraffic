package gui;

import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimpleModel;

public class Model extends SimpleModel {

	private int numberOfAgents;

	public Model() {
		name = "My Hello World Model";
	}

	public void setup() {
		super.setup();
		numberOfAgents = 3;
		autoStep = true;
		shuffle = true;
	}

	public void buildModel() {
		for(int i=0; i<numberOfAgents; i++)
			agentList.add(new Agent(i));
	}

	protected void preStep() {
		System.out.println("Initiating step " + getTickCount());
	}

	protected void postStep() {
		System.out.println("Done step " + getTickCount());
	}


	public static void main(String[] args) {
		SimInit init = new SimInit();
		Model model = new Model();
		init.loadModel(model, null, true);
	}

}