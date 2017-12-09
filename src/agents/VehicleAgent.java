package agents;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JOptionPane;

import sajas.core.Agent;
import sajas.core.behaviours.*;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.RectNetworkItem;
import agents.TrafficLightAgent;
import behaviours.EncounterCar;
import behaviours.EncounterTrafficLight;
import behaviours.FindTrafficLights;
import graph.Graph;
import graph.MyNode;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

@SuppressWarnings("serial")
public class VehicleAgent extends Agent{

	private static int IDNumber=0;
	private int ID;
	private int[] position = new int[2];	//posiçao atual do carro
	private int[] nextPosition = new int[2]; //posiçao seguinte do carro
	private Vector<VehicleAgent> cars;
	private Vector<TrafficLightAgent> trafficLights;
	public VehicleAgent car = this;
	private AID lightAtCarPos;
	private int step;
	private int velocity;
	private RectNetworkItem s;
	private DisplaySurface disp;
	private Graph graph;
	private ArrayList<MyNode> carsNodes;
	private MyNode n;
	private boolean accident;
	private int numAccidents;
	private int repliesCnt;
	private boolean foundCar;
	private ACLMessage reply;
	private MessageTemplate mt;
	Behaviour searchLight, dealLight, encounterCar;


	//para apagar
	private int[] xtrajetoriaV1 = new int[7];
	private int[] ytrajetoriaV1 = new int[7];
	private int[] xtrajetoriaV2 = new int[5];
	private int[] ytrajetoriaV2 = new int[5];
	private int index = 0;


	public VehicleAgent(int x, int y, int velocity, Vector<VehicleAgent> cars, Vector<TrafficLightAgent> trafficLights, Graph graph, ArrayList<MyNode> carsNodes, DisplaySurface disp) {
		IDNumber++;
		ID=IDNumber;
		this.cars = cars;
		this.trafficLights = trafficLights;
		position[0] = x;
		position[1] = y;
		this.velocity = velocity;
		this.accident = false;
		this.numAccidents = 0;
		this.s= new RectNetworkItem(x,y);
		this.disp=disp;
		Color[] cores = new Color[4];
		cores[0] = Color.BLUE;
		cores[1] = Color.CYAN;
		cores[2] = Color.PINK;
		cores[3] = Color.YELLOW;
		java.util.Random r = new java.util.Random();
		int iCor = r.nextInt(4);
		s.setColor(cores[iCor]);
		this.carsNodes = carsNodes;
		this.graph = graph;
		n = new MyNode(getS(),getX(),getY());
		this.carsNodes.add(n);

		//para apagar
		xtrajetoriaV1[0] = 70+30;
		xtrajetoriaV1[1] = 85+30;
		xtrajetoriaV1[2] = 100+30;
		xtrajetoriaV1[3] = 115+30;
		xtrajetoriaV1[4] = 130+30;
		xtrajetoriaV1[5] = 145+30;
		xtrajetoriaV1[6] = 160+30;

		ytrajetoriaV1[0] = 110;
		ytrajetoriaV1[1] = 110;
		ytrajetoriaV1[2] = 110;
		ytrajetoriaV1[3] = 110;
		ytrajetoriaV1[4] = 110;
		ytrajetoriaV1[5] = 110;
		ytrajetoriaV1[6] = 110;

		/*xtrajetoriaV2[0] = 100;
		xtrajetoriaV2[1] = 115;
		xtrajetoriaV2[2] = 130;
		xtrajetoriaV2[3] = 145;
		xtrajetoriaV2[4] = 160;
		xtrajetoriaV2[5] = 175;
		xtrajetoriaV2[6] = 190;

		ytrajetoriaV2[0] = 110;
		ytrajetoriaV2[1] = 110;
		ytrajetoriaV2[2] = 110;
		ytrajetoriaV2[3] = 110;
		ytrajetoriaV2[4] = 110;
		ytrajetoriaV2[5] = 110;
		ytrajetoriaV2[6] = 110;*/

		xtrajetoriaV2[0] = 100+60;
		xtrajetoriaV2[1] = 100+60;
		xtrajetoriaV2[2] = 100+60;
		xtrajetoriaV2[3] = 100+60;
		xtrajetoriaV2[4] = 100+60;
		//xtrajetoriaV2[5] = 100;
		//xtrajetoriaV2[6] = 100;

		//ytrajetoriaV2[0] = 70;
		//ytrajetoriaV2[1] = 80;
		ytrajetoriaV2[0] = 90;
		ytrajetoriaV2[1] = 100;
		ytrajetoriaV2[2] = 110;
		ytrajetoriaV2[3] = 120;
		ytrajetoriaV2[4] = 130;
	}

	public RectNetworkItem getS() {
		return s;
	}

	public void setS(RectNetworkItem s) {
		this.s = s;
	}

	public void setLightAtCarPos(AID light){
		lightAtCarPos = light;
	}

	public int getID() {
		return ID;
	}

	private int getX() {
		return position[0];
	}

	private int getY() {
		return position[1];
	}

	public int[] getPosition() {
		return position;
	}

	public int[] getNextPosition() {
		return nextPosition;
	}

	public void updateDisplayCar(){

		carsNodes.remove(n);
		n = new MyNode(this.getS(),this.getX(),this.getY());
		carsNodes.add(n);
		s.setX(getX());
		s.setY(getY());
		if(accident == true){
			s.setColor(Color.BLACK);
		}
		disp.updateDisplay();
	}


	protected void setup() {

		System.out.println("Hello! Vehicle-Agent "+ getAID().getName() + " is ready.");

		step = 0;
		lightAtCarPos = null;
		addBehaviour(new TickerBehaviour(this, velocity){

			@Override
			protected void onTick() {  

				System.out.println("car " + getAID().getName()+ " position: " + position[0] + position [1]);
				String strCarPos = "" + car.getPosition()[0] + car.getPosition()[1] + "";

				//carro ve se tem semaforo
				switch (step){

				case 0:
					//send the cfp to all cars
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					for(int i = 0; i < cars.size(); i++){
						if(cars.elementAt(i).getAID() != car.getAID()){
							cfp.addReceiver(cars.elementAt(i).getAID());
						}
					}
					cfp.setContent("position");
					cfp.setConversationId("position");
					cfp.setReplyWith("cfp"+System.currentTimeMillis());
					car.send(cfp);
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("position"), MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));			

					repliesCnt = 0;
					foundCar = false;
					step = 1;
					break;
				case 1:
					//receive all answers from cars
					reply = car.receive(mt); 
					if(reply != null){
						if(reply.getContent().equals(strCarPos)){
							foundCar = true;
							accident = true;
							numAccidents++;
						}
						repliesCnt++;
						if(repliesCnt == cars.size()-1){
							step = 2;
						}
					}
					else{
						block();
					}
					break;
				case 2:
					//perguntar a todos os semaforos a posiçao
					searchLight = new FindTrafficLights(car, trafficLights);
					addBehaviour(searchLight);
					step = 3;
					break;
				case 3:
					if (searchLight.done()){
						step = 4;
					}
					break;
				case 4:
					//perguntar cor do semaforo que encontrou
					if(lightAtCarPos != null){
						dealLight = new EncounterTrafficLight(car, lightAtCarPos);
						addBehaviour(dealLight);
						step = 5;

					} else{
						step = 6;
					}
					break;
				case 5:
					if(dealLight.done()){
						step = 6;
					}
					else{
						block();
					}
					break;

				case 6:
					//TODO hardcoded vai ser para mudar para mover no grafo
					if(getAID().getName().equals("Vehicle1@City Traffic")){
						nextPosition[0] = xtrajetoriaV1[index];
						nextPosition[1] = ytrajetoriaV1[index];
					}
					else{
						nextPosition[0] = xtrajetoriaV2[index];
						nextPosition[1] = ytrajetoriaV2[index];
					}
					index++;
					encounterCar = new EncounterCar(car, cars);
					addBehaviour(encounterCar);
					step = 7;
					break;
				case 7:
					if(encounterCar.done()){
						step = 8;
					}
					else{
						block();
					}
					break;
				case 8:

					//position[0] = position[0] + 1;
					//position[1] = position[1] + 1;
					//TODO (2) eventualmente faze lo andar pelos pontos do grafo
					//para já andam random, depois andam pelo caminho até ao destino
					//para testar vou por aqui caminha harcoded

					position[0] = nextPosition[0];
					position[1] = nextPosition[1];

					updateDisplayCar();

					step = 0;
					repliesCnt = 0;
					foundCar = false;
					break;
				}

				//TODO (5)carro para o tick behavior se tiver chegado ao destino (ou seja implica criar posiçoes iniciais e finas e faze lo percorrer o caminha, implica implementar djkistra
			}
		});


		addBehaviour(new CyclicBehaviour(){

			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = receive(mt);
				if(msg != null){
					ACLMessage reply = msg.createReply();
					if(msg.getPerformative() == ACLMessage.CFP){
						if(msg.getConversationId().equals("position")){
							reply.setPerformative(ACLMessage.INFORM);
							String pos = "" + position[0] + position[1] + "";
							reply.setContent(pos);
							reply.setConversationId("position");
							car.send(reply);
						}
					}
				}
			}

		});

	}

	protected void takeDown(){
		// Printout a dismissal	message
		System.out.println("Vehicle-Agent "+getAID().getName()+ "terminating.");
	}
}