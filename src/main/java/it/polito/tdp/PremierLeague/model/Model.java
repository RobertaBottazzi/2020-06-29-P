package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private SimpleWeightedGraph<Match, DefaultWeightedEdge> grafo;
	private Map<Integer, Match> idMap;
	private PremierLeagueDAO dao;
	private List<Match> percorsoMigliore;
	private double massimo;
	
	public Model() {
		this.dao=new PremierLeagueDAO();
		this.idMap= new HashMap<>();
		this.dao.loadAllMatches(idMap);
	}
	
	public void creaGrafo(int month, int minuti) {
		this.grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		Graphs.addAllVertices(this.grafo, this.dao.listAllMatchesPerMonth(idMap, month));
		for(Arco a: this.dao.getArchi(idMap, minuti, month)) {
			if(a.getPeso()>0)
				Graphs.addEdgeWithVertices(this.grafo, a.getM1(), a.getM2(), a.getPeso());
		}		
	}
	
	public List<Arco> getConnessioneMax(int minuti){
		double massimo=0;
		Arco connMax = null;
		List<Arco> connessioni= new ArrayList<>();
		for(DefaultWeightedEdge edge: this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(edge)>massimo) {
				massimo= this.grafo.getEdgeWeight(edge);
			}
		}
		//elenca tutti gli archi con peso massimo
		for(DefaultWeightedEdge edge: this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(edge)==massimo) {
				connMax= new Arco(this.grafo.getEdgeSource(edge), this.grafo.getEdgeTarget(edge),(int)(this.grafo.getEdgeWeight(edge)));
				connessioni.add(connMax);
			}
		}
		return connessioni;
	}
	
	public SimpleWeightedGraph<Match, DefaultWeightedEdge> getGrafo(){
		return grafo;
	}
	
	public List<Match> trovaPercorsoMigliore(Match partenza, Match arrivo){
		this.percorsoMigliore=new ArrayList<>();
		this.massimo=0.0;
		List<Match> parziale= new ArrayList<>();
		parziale.add(partenza);
		cerca(arrivo,parziale);
		return percorsoMigliore;
	}
	
	private void cerca(Match arrivo,List<Match> parziale) {
		//caso terminale
		if(parziale.get(parziale.size()-1).equals(arrivo) && calcolaPeso(parziale)>this.massimo) {
			this.percorsoMigliore=new ArrayList<>(parziale);
			this.massimo=calcolaPeso(parziale);
			return;			
		}
		//genero i percorsi, sono in un vertice e devo andare a trovare tutti i vertici vicini
		Match ultimo=parziale.get(parziale.size()-1);
		for(Match m: Graphs.neighborListOf(this.grafo, ultimo)) {
			if(!parziale.contains(m) && (m.teamHomeID!=ultimo.teamHomeID && m.teamAwayID!=ultimo.teamAwayID && m.teamHomeID!=ultimo.teamAwayID && m.teamAwayID!=ultimo.teamHomeID) ) {
				parziale.add(m);
				cerca(arrivo,parziale);
				parziale.remove(parziale.size()-1);
			}
		}
	}
	
	private double calcolaPeso(List<Match> parziale) {
		double peso=0.0;
		for(int i=0; i<parziale.size();i++ ) {
			peso+=this.grafo.getEdgeWeight(this.grafo.getEdge(parziale.get(i-1), parziale.get(i)));
		}
		return peso;
	}
}
