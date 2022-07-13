package it.polito.tdp.yelp.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.yelp.model.Event.EventType;


public class Simulator {

	//dati in input
	private int numUtenti;
	private int numIntervistatori;
	private Graph<User,DefaultWeightedEdge> grafo;
	
	//dati in uscita
	private int numeroGiorni;
	private List<Giornalista> giornalisti ;
	
	//modello del mondo
	private Set<User> intervistati ;
	
	// Coda degli eventi
	private PriorityQueue<Event> queue ;
	
	public Simulator(Graph<User,DefaultWeightedEdge> grafo) {
		this.grafo = grafo ;
	}
	
	public void init(int x1, int x2) {
		this.numUtenti = x2;
		this.numIntervistatori = x1;
		
		this.numeroGiorni = 0;
		this.giornalisti = new ArrayList<>();
		
		for(int i=0;i<this.numIntervistatori;i++) {
			this.giornalisti.add(i, new Giornalista(i));
		}
		
		this.intervistati = this.grafo.vertexSet();
		
		this.queue = new PriorityQueue<>();
		
		for(Giornalista g: this.giornalisti) {
			User intervistato = selezionaIntervistato(this.grafo.vertexSet()) ;
			
			this.intervistati.add(intervistato);
			g.incrementaNumeroIntervistati();
			
			this.queue.add(new Event(1, EventType.DA_INTERVISTARE, intervistato, g));
		}
	}
	
	public void run() {
		while(!this.queue.isEmpty() && this.intervistati.size()<numUtenti) {
			Event e = this.queue.poll() ;
			this.numeroGiorni = e.getGiorno();
			
			processEvent(e);
		}
	}

	private void processEvent(Event e) {
		switch (e.getType()) {
		case DA_INTERVISTARE:
			double caso = Math.random();
			
			if(caso<0.6) {
				User intervistato = selezionaAdiacente(e.getIntervistato()) ;
				
				if(intervistato != null) {
					this.intervistati.add(intervistato);
					e.getGiornalista().incrementaNumeroIntervistati();
					
					this.queue.add(new Event(e.getGiorno()+1, EventType.DA_INTERVISTARE, intervistato, e.getGiornalista()));
				}else {
					intervistato = selezionaIntervistato(this.grafo.vertexSet());
					this.intervistati.add(intervistato);
					e.getGiornalista().incrementaNumeroIntervistati();
					
					this.queue.add(new Event(e.getGiorno()+1, EventType.DA_INTERVISTARE, intervistato, e.getGiornalista()));
				}
			}else if(caso<0.8) {
				this.queue.add(new Event(e.getGiorno()+1,EventType.FERIE, e.getIntervistato(),e.getGiornalista()));
			}else {
				this.queue.add(new Event(e.getGiorno()+1,EventType.DA_INTERVISTARE, e.getIntervistato(),e.getGiornalista()));
			}
			break;
			
		case FERIE:
			User intervistato = selezionaAdiacente(e.getIntervistato()) ;
			
			if(intervistato != null) {
				this.intervistati.add(intervistato);
				e.getGiornalista().incrementaNumeroIntervistati();
				
				this.queue.add(new Event(e.getGiorno()+1, EventType.DA_INTERVISTARE, intervistato, e.getGiornalista()));
			}else {
				intervistato = selezionaIntervistato(this.grafo.vertexSet());
				this.intervistati.add(intervistato);
				e.getGiornalista().incrementaNumeroIntervistati();
				
				this.queue.add(new Event(e.getGiorno()+1, EventType.DA_INTERVISTARE, intervistato, e.getGiornalista()));
			}
			break;
		}
		
	}

	private User selezionaIntervistato(Set<User> lista) {
		Set<User> candidati = new HashSet<User>(lista);
		candidati.removeAll(this.intervistati);
		
		if (candidati.size()==0)
			return null;
		
		int scelto = (int)(Math.random()*candidati.size());
		
		return (new ArrayList<User>(candidati)).get(scelto) ;
	}
	
	
	private User selezionaAdiacente(User u) {
		List<User> vicini = Graphs.neighborListOf(this.grafo, u);
		vicini.removeAll(this.intervistati);
		
		if(vicini.size()==0) {
			// vertice isolato
			// oppure tutti adiacenti già intervistati
			return null ;
		}
		
		double max = 0;
		for(User v: vicini) {
			double peso = this.grafo.getEdgeWeight(this.grafo.getEdge(u, v)); 
			if(peso > max)
				max = peso ;
		}
		
		List<User> migliori = new ArrayList<>();
		for(User v: vicini) {
			double peso = this.grafo.getEdgeWeight(this.grafo.getEdge(u, v)); 
			if(peso == max) {
				migliori.add(v) ;
			}
		}
		
		int scelto = (int)(Math.random()*migliori.size()) ;
		return migliori.get(scelto);
	}
	
	public int getNumIntervistatori() {
		return this.numIntervistatori;
	}

	public void setNumIntervistatori(int x1) {
		this.numIntervistatori = x1;
	}

	public int getNumUtenti() {
		return this.numUtenti;
	}

	public void setNumeUtenti(int x2) {
		this.numUtenti = x2;
	}

	public List<Giornalista> getGiornalisti() {
		return giornalisti;
	}

	public int getNumeroGiorni() {
		return numeroGiorni;
	}
}
