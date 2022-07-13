package it.polito.tdp.yelp.model;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	
	private Graph<User, DefaultWeightedEdge> grafo;
	private List<User> listaUtenti;
	
	private int numeroGiorni ;
	private List<Giornalista> giornalisti ;
	
	public Model() {
		
	}
	public String creaGrafo(int minRevisioni, int anno) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		YelpDao dao = new YelpDao();
		
		this.listaUtenti = dao.getUsersWithReviews(minRevisioni);
		Graphs.addAllVertices(this.grafo, this.listaUtenti);
		
		for (User u1 : this.listaUtenti) {
			for(User u2 : this.listaUtenti) {
				if(!u1.equals(u2) && u1.getUserId().compareTo(u2.getUserId())<0) {
					int sim = dao.calcolaSimlarita(u1, u2, anno);
					if(sim>0) {
						Graphs.addEdgeWithVertices(this.grafo, u1, u2, sim);
					}
				}
			}
		}
		
		System.out.println("#VERTICI: " + this.grafo.vertexSet().size());
		System.out.println("#ARCHI: " + this.grafo.edgeSet().size());
		
		return "Grafo creato con " + this.grafo.vertexSet().size() + " vertici e " + this.grafo.edgeSet().size() + " archi\n";
	}
	
	public List<User> getUsers(){
		return this.listaUtenti;
	}
	
	public List<User> utentiPiuSimili(User u){
		int max = 0 ;
		for(DefaultWeightedEdge e : this.grafo.edgesOf(u)) {
			if(this.grafo.getEdgeWeight(e)>max) {
				max = (int) this.grafo.getEdgeWeight(e);
			}
		}
		
		List<User> result = new ArrayList<>();
		for(DefaultWeightedEdge e : this.grafo.edgesOf(u)) {
			if((int) this.grafo.getEdgeWeight(e)==max) {
				User u2 = Graphs.getOppositeVertex(this.grafo, e, u);
				result.add(u2);
			}
		}
		
		return result;
	}
	
	public void simula(int intervistatori, int utenti) {
		Simulator sim = new Simulator(this.grafo);
		sim.init(intervistatori, utenti);
		sim.run();
		this.giornalisti = sim.getGiornalisti();
		this.numeroGiorni = sim.getNumeroGiorni();
	}
	

	public int getNumeroGiorni() {
		return numeroGiorni;
	}

	public List<Giornalista> getGiornalisti() {
		return giornalisti;
	}
}
