package org.protege.owl.axiome.ds;
/*
 * utilisation des BI qui se trouvent dans le head (recherche d'IPs possibles entre
 * les args des atomes comparés) 
 * 
 * algorithme 3 //exsistence d'une IP entre des types des algs ayant la mm position
 * 
 * algorithme 4 //transitivité 
 * 
 * groupement de Hassapoor, sans recours à la sémantique
 * 
 * tenir compte de downward link et upward link (classement manuel des IPs)
 * 
 * Alg6: supprimer les dépendances entre les règles du mm groupe 
 * 
 * alg 3.5 suppression des cycles binaires par anyIPsWithDirection
 * 
 * alg 8 : extraction des dépendances avec anyIPsWithDirection seulement
 * 
 * alg 7: filtrage de la matrice par anyIPsWithDirection
 * 
 * alg9: suppression des cycles 
 * 
 */
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.io.*;

import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;



import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLClassAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLDatavaluedPropertyAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLFactory;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLImp;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLIndividualPropertyAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLBuiltinAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLVariable;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLDifferentIndividualsAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLSameIndividualAtom;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.*;
import edu.uci.ics.jung.algorithms.cluster.*;
//import edu.uci.ics.jung.algorithms.importance.PageRank;

/*import com.nastra.algorithms.graph.CycleFinder;
import com.nastra.algorithms.graph.TopologicalSorter;
import com.nastra.datastructures.Digraph;


import de.normalisiert.utils.graphs.*;
*/
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;



import org.apache.commons.collections15.list.TreeList;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.protege.owl.axiome.ds.JoinRules.TestTreeTable.Rule;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.DirectedGraph; 
import org.jgrapht.alg.cycle.*;

public class JoinRules {
	int nb1(int[][] m){
		int n=0;
		for(int i=0; i<m.length; i++)
			for(int j=0; j<m.length; j++)
				if(m[i][j]!=0)
					n++;
		return n;
	}
	///////////////////////////////////////
	//String reasons="";
	public boolean inTab(int x, int[] tab){
		for(int i=0; i<tab.length; i++)
			if(tab[i]==x)
				return true;
		return false;
	}
	public void inFile(String fichier, String chaine, boolean append){
		
		PrintWriter fluxSortie = null;
		 try
		 {
		 fluxSortie =
		 new PrintWriter(new FileOutputStream(fichier,append));
		 }
		 catch(FileNotFoundException e)
		 { System.out.println("Erreur d’ouverture"+ fichier);
		 }
		
		 fluxSortie.print(chaine);
		 
		 fluxSortie.close( );
		 
	}
public void inFileln(String fichier, String chaine, boolean append){
		
		PrintWriter fluxSortie = null;
		 try
		 {
		 fluxSortie =
		 new PrintWriter(new FileOutputStream(fichier,append));
		 }
		 catch(FileNotFoundException e)
		 { System.out.println("Erreur d’ouverture"+ fichier);
		 }
		
		 fluxSortie.println(chaine);
		 
		 fluxSortie.close( );
		 
	}

	void afficheCollection(Collection<RDFResource> c){
		//String s="";
		Iterator it = c.iterator() ;
		
		 while (it.hasNext()) {
			 Object o = it.next();
		    System.out.println(" -> "+((RDFResource)o).getBrowserText()) ;
		    
		}
		 
	}
		
	int getRuleNum(String r){
		
		int nr = -1;
		try{
			nr = Integer.parseInt(r.substring(5,r.length()));
			
		}catch(Exception ex){}
		return nr;
}
boolean subOf(RDFSClass c1, RDFSClass c2){
		
		return(c2.getSubclasses().contains(c1));		
	}
boolean subOf(Slot s1, Slot s2){
	return(s2.getDirectSubslots().contains(s1));	
}
boolean areEquivalent(DefaultOWLDatatypeProperty rb, DefaultOWLDatatypeProperty rh){
	Collection cc=(rb).getSameAs();
	
	if(cc.contains(rh))
	return true;
	
	return false;
}

boolean areEquivalent(DefaultOWLObjectProperty rb, DefaultOWLObjectProperty rh){
	Collection cc1=(rb).getSameAs();
	
	if(cc1.contains(rh))
	return true;
	
	return false;
}

boolean areEquivalent(RDFSClass c1, RDFSClass c2){
	Collection cc1=((OWLClass)c1).getEquivalentClasses();
	if(cc1.contains(c2))
	return true;
	return false;
}

boolean equalOrEquivalent(RDFSClass c1, RDFSClass c2){
	return(c1.equals(c2) //mm classe
			|| areEquivalent(c1, c2));
}

boolean equalOrEquivalent(DefaultOWLObjectProperty p1, DefaultOWLObjectProperty p2){
	return(p1.equals(p2) //mm classe
			|| areEquivalent(p1, p2)
			);
}

boolean equalOrEquivalent(DefaultOWLDatatypeProperty p1,
		DefaultOWLDatatypeProperty p2) {
	return (p1.equals(p2) //mm propriété
	|| areEquivalent((DefaultOWLDatatypeProperty)p1, (DefaultOWLDatatypeProperty)p2)//équivalence
	);
}

boolean equalOrEquivalentOrSubOf(RDFSClass c1, RDFSClass c2){
	return(c1.equals(c2) //mm classe
				|| areEquivalent(c1, c2)
				||subOf(c1,c2));
}
boolean equalOrEquivalentOrSubOf(DefaultOWLObjectProperty p1, DefaultOWLObjectProperty p2){
	return(p1.equals(p2) //mm classe
			|| areEquivalent(p1, p2)
			||subOf(p1,p2));
}
boolean equalOrEquivalentOrSubOf(DefaultOWLDatatypeProperty p1,
		DefaultOWLDatatypeProperty p2) {
	return (p1.equals(p2) //mm propriété
	|| areEquivalent((DefaultOWLDatatypeProperty)p1, (DefaultOWLDatatypeProperty)p2)//équivalence
	||subOf((DefaultOWLDatatypeProperty)p1, (DefaultOWLDatatypeProperty)p2));
}

Vector<SWRLAtom> getVarAtoms(SWRLVariable v,ArrayList<RDFResource> atoms/*, OWLModel owlModel*/){
	Vector<SWRLAtom> atomsVar = new Vector();
	SWRLAtom a;
	String rdfType;
	/*System.out.println("atoms: ");
	afficheCollection((Collection)atoms);*/
	//System.out.println("-------------atoms-----------------");
	for(int i = 0; i<atoms.size(); i++){
		a = (SWRLAtom)atoms.get(i);
		rdfType = a.getRDFType().getBrowserText();
		//System.out.println(a.getBrowserText()+"----->"+rdfType);
		if(rdfType.equals("swrl:ClassAtom")){
			if( ((SWRLClassAtom)a).getArgument1().equals(v)){
			atomsVar.add(a);
		}
		}else
			if(rdfType.equals("swrl:IndividualPropertyAtom")){
				boolean arg1=false, arg2=false;
				try{
					arg1 = ((SWRLVariable)((SWRLIndividualPropertyAtom)a).getArgument1()).equals(v);
				}catch(Exception e){}
				try{
					arg2 = ((SWRLVariable)((SWRLIndividualPropertyAtom)a).getArgument2()).equals(v);
				}catch(Exception e){}
				
				if(arg1	|| arg2){
					atomsVar.add(a);
				}
			}else
				if(rdfType.equals("swrl:DatavaluedPropertyAtom")){
					boolean arg1=false;
					try{
						arg1 = ((SWRLVariable)((SWRLDatavaluedPropertyAtom)a).getArgument1()).equals(v);
					}catch(Exception e){}
						if(arg1){
					atomsVar.add(a);
				}
				}
	}
	//System.out.println("------------------------------");
	return atomsVar;
}


Vector<RDFSClass> getVarTypeFromAtom(SWRLVariable v,SWRLAtom a, OWLModel owlModel){
	Vector types = new Vector();
	types.add((RDFSClass)owlModel.getRootCls());// type = Thing)
	String rdfType = a.getRDFType().getBrowserText();
	Slot slot;
	RDFSClass tmpCls1, tmpCls2;
	if(rdfType.equals("swrl:ClassAtom")&& ((SWRLClassAtom)a).getArgument1().equals(v)){
		types = new Vector();
		types.add((((SWRLClassAtom)a)).getClassPredicate());
		
	}
		else
			if(rdfType.equals("swrl:IndividualPropertyAtom")){
				if(((SWRLVariable)((SWRLIndividualPropertyAtom)a).getArgument1()).equals(v)){
					slot=	owlModel.getOWLProperty(((SWRLIndividualPropertyAtom)a).getPropertyPredicate().getBrowserText());
					types = new Vector();
					types.addAll( owlModel.getDirectDomain(slot));//System.out.println("remplir c 1="+c.size());
				}else
					if(((SWRLVariable)((SWRLIndividualPropertyAtom)a).getArgument2()).equals(v)){
							slot=	owlModel.getOWLProperty(((SWRLIndividualPropertyAtom)a).getPropertyPredicate().getBrowserText());
							types = new Vector();
							types.addAll( owlModel.getAllowedClses(slot));//System.out.println("remplir c 2="+c.size());
					}
			}else
				if(rdfType.equals("swrl:DatavaluedPropertyAtom")){
					if(((SWRLVariable)((SWRLDatavaluedPropertyAtom)a).getArgument1()).equals(v)){
						slot=	owlModel.getOWLProperty(((SWRLDatavaluedPropertyAtom)a).getPropertyPredicate().getBrowserText());
						types = new Vector();
						types.addAll( owlModel.getDirectDomain(slot));
				}
				}	
	
	return types;
}

/*TODO*/
/*private Vector<RDFSClass> getVarType(String label, SWRLVariable v, ArrayList<RDFResource> body, ArrayList<RDFResource> head, String part, OWLModel owlModel){
ArrayList<RDFResource> atoms;
	
	atoms=body; 
	if(part.equals("head")){
		Vector<RDFSClass>	
		
		
	}else{
		
	}
}*/
private Vector<RDFSClass> getVarType(String label, RDFResource r, ArrayList<RDFResource> body, ArrayList<RDFResource> head, String part, OWLModel owlModel){
	Vector<RDFSClass> types=new Vector<RDFSClass>();
	SWRLVariable v;
RDFSClass cls = r.getRDFType();
if(r.getClass().getSimpleName().equals("DefaultSWRLVariable")){
	v = (SWRLVariable)r;
}else{
	types.add(r.getRDFType());
	//System.out.println(r.getBrowserText()+" : "+r.getRDFType().getBrowserText());
	return types;
}
	
/*if(r.getClass().getSimpleName().equals("DefaultRDFSNamedClass")){
		Carg.addElement((RDFSClass) r);
	}else
		if(r.getClass().getSimpleName().equals("DefaultSWRLVariable")){
			Carg = getVarType(label[i],(SWRLVariable)r, body[i], head[i], "head",owlModel);
		}*/
	Vector<RDFSClass> tmps=new Vector<RDFSClass>();
	ArrayList<RDFResource> atoms;
	Vector<SWRLAtom> varAtoms = new Vector();
	
	atoms=body; 
	if(part.equals("head")){
		//atoms.addAll(head);
		varAtoms = getVarAtoms(v, body);
		atoms=head;
	}
		
	RDFSClass tmpCls1, tmpCls2;
	
	Vector<RDFSClass> c = new Vector();
	//System.out.println(part+ ": "+v.getBrowserText());
	varAtoms.addAll(getVarAtoms(v, atoms));
	if(varAtoms.size()==0){
		if(part.equals("head")){
			return getVarType(label,v,  body, head,  "body", owlModel);
		}else{
		
			types.add((RDFSClass)owlModel.getRootCls());
			return types;
		}
		
	}else
		if(varAtoms.size()==1){
			types = getVarTypeFromAtom(v, varAtoms.firstElement(), owlModel);
			
			if((types.size()==1 && types.firstElement().equals((RDFSClass)owlModel.getRootCls()))||types.size()==0){
				// ex: head est un seul atome de type IP dont le type de v est owl:Thing
				if(part.equals("head"))
					return getVarType(label, v,  body, head,  "body", owlModel);
				
			}
					
			return types;
			}
			
	
	
	/* search:{for(int p=0; p<1; p++){//un goto pour le cas où : après la recherche du type de l'arg on a obtenu seulement owl:Thing
	 								//!!!! possibilité de boucle infini (head, body, head, body ....
			System.out.println("*********goto");*/
	
			types = getVarTypeFromAtom(v, varAtoms.firstElement(), owlModel);//types possibles dans atomsVar[0]
			
			for(int k=1; k< varAtoms.size(); k++){
				c = new Vector<RDFSClass>();
				tmps = getVarTypeFromAtom(v, varAtoms.elementAt(k), owlModel);//types possibles de v dans atomsVar[k]
				
				for(int i = 0; i<types.size(); i++){
					for(int j=0; j< tmps.size(); j++){
						tmpCls1 = types.elementAt(i);
						tmpCls2 = tmps.elementAt(j);
						if(tmpCls1.equals(tmpCls2)||subOf(tmpCls1, tmpCls2))
							c.add(tmpCls1);
						else
							if( subOf(tmpCls2, tmpCls1)){
								types.set(i, tmpCls2);//garder la classe la plus basse dans l'hiérarchie de l'ontologie
								c.add(tmpCls2);
							}
						
					}
				}
				if(c.size()>0)
				types = c;
			}//for k
			
			/*//le reste du goto
			 if(c.size()==1 && c.firstElement().equals((RDFSClass)owlModel.getRootCls())){
				System.out.println("** c = owl:Thing");
				if(part.equals("head")){
					varAtoms = getVarAtoms(v, body);
					varAtoms.addAll(getVarAtoms(v, head));
					done = true;	
					System.out.println("*********goto");
					break search;
				}
			}
	}
	}	
	*/
			return types;
	
}
	
Boolean equalOrEquivalent(Vector<RDFSClass> argh,Vector<RDFSClass> argb ){
	for(int i=0; i<argh.size(); i++)
		for(int j=0; j<argb.size(); j++){
			if(equalOrEquivalent(argh.elementAt(i),argb.elementAt(j)))
				return true;
		}
	return false;
}

Boolean subOf(Vector<RDFSClass> argh,Vector<RDFSClass> argb ){
	for(int i=0; i<argh.size(); i++)
		for(int j=0; j<argb.size(); j++){
			if(subOf(argh.elementAt(i),argb.elementAt(j)))
				return true;
		}
	return false;
}

Boolean dependsOn(Vector<RDFSClass> argh,Vector<RDFSClass> argb ){
	for(int i=0; i<argh.size(); i++)
		for(int j=0; j<argb.size(); j++){
			if(equalOrEquivalent(argh.elementAt(i),argb.elementAt(j)))
				return true;
		}
	return false;
}

int[] path;// = new int[size];
//verrou pour la recherche taboo (tous initialisés à "false" par défaut)
boolean[] taboo;

/*TODO modif1 alg4 transitivité
 * 
 */

void modif3(int source, int target, int position, int depth) {//1->2->3   &&  1-->3 : supprimer 1-->3
	
	path[depth]=position;
	// on est sur le sommet d'arrivé -> fini
	if (position==target) {
		if(depth>1 && groupGraph[source][target]!=0)
		
			groupGraph[source][target]=0;	
		return;
	}
 
	// sinon...
 
	taboo[position]=true; // on pose un caillou
 
	// on explore les chemins restants
	for(int i=0;i<groupGraph.length;i++) {
		if (groupGraph[position][i]==0 || taboo[i]) continue;
		
		modif3(source, target,i,depth+1);
	}
 
	taboo[position]=false; // on retire le caillou
}

void modif11(int source, int target, int position, int depth) {//1->2->3   &&  1-->3 : supprimer 1-->3
	
	path[depth]=position;
	// on est sur le sommet d'arrivé -> fini
	if (position==target) {
		if(depth>1 && grpsGrph[source][target]!=0)
		
			grpsGrph[source][target]=0;	
		return;
	}
 
	// sinon...
 
	taboo[position]=true; // on pose un caillou
 
	// on explore les chemins restants
	for(int i=0;i<grpsGrph.length;i++) {
		if (grpsGrph[position][i]==0 || taboo[i]) continue;
		
		modif11(source, target,i,depth+1);
	}
 
	taboo[position]=false; // on retire le caillou
}


void modif4(int source, int target, int position, int depth) {//1->2->3   &&  1-->3 : supprimer 1-->3
	
	path[depth]=position;
	// on est sur le sommet d'arrivé -> fini
	if (position==target) {
		if(depth>1 && myMatrix8[source][target]!=0)
		
			myMatrix8[source][target]=0;	
		return;
	}
 
	// sinon...
 
	taboo[position]=true; // on pose un caillou
 
	// on explore les chemins restants
	for(int i=0;i<myMatrix8.length;i++) {
		if (myMatrix8[position][i]==0 || taboo[i]) continue;
		
		modif4(source, target,i,depth+1);
	}
 
	taboo[position]=false; // on retire le caillou
}


void modif2(int source, int target, int position, int depth) {//transitivité sur myMatrixAlg5
	
	path[depth]=position;
	// on est sur le sommet d'arrivé -> fini
	if (position==target) {
		
		if(depth>0 && myMatrix[source][target]!=0){
			myMatrix[source][target]=0;/*TODO MODIFIER edgeProp*/
			try{edgeProp[source][target].remove(2);
			
			}catch(Exception ex){}
			//System.out.println("@-"+depth);
		}
		//s'il s'agit d'un cycle, supprimer le dernier arc
		if(myMatrix[path[depth]][source]!=0){
			myMatrix[path[depth]][source]=0;
		}
		return;
	}
 
	// sinon...
 
	taboo[position]=true; // on pose un caillou
 
	// on modif1 les chemins restants
	for(int i=0;i<size;i++) {
		if (myMatrix[position][i]==0 || taboo[i]) continue;
		
		modif2(source, target,i,depth+1);
	}
 
	taboo[position]=false; // on retire le caillou
}


void modif1(int source, int target, int position, int depth) {
	
	path[depth]=position;
	// on est sur le sommet d'arrivé -> fini
	if (position==target) {
		
		if(depth>0 && grpsGrph[source][target]==0)
			grpsGrph[source][target]=0;
			//System.out.println("@-"+depth);
		
		//s'il s'agit d'un cycle, supprimer le dernier arc
		if(grpsGrph[path[depth]][source]!=0){
			grpsGrph[path[depth]][source]=0;
		}
		return;
	}
 
	// sinon...
 
	taboo[position]=true; // on pose un caillou
 
	// on explore les chemins restants
	for(int i=0;i<grpsGrph.length;i++) {
		if (grpsGrph[position][i]==0 || taboo[i]) continue;
		
		modif1(source, target,i,depth+1);
	}
 
	taboo[position]=false; // on retire le caillou
}


boolean anyIPs(Vector<RDFSClass> typesArgH, Vector<RDFSClass> typesArgB, OWLModel owlModel){

	Vector<RDFSClass> arg1, arg2;
	RDFProperty slot;
	
		arg1= typesArgH;
		arg2= typesArgB;
	
	Collection c ;//les types possibles de l'argument
	Collection range, supCls;
		for(int h=0; h<arg1.size(); h++){
			c = owlModel.getDirectTemplateSlots(arg1.elementAt(h));//les IP dont le domaine est le type en cours
			for(int b=0; b<arg2.size(); b++){
			for(Iterator<Slot> itc = c.iterator(); itc.hasNext();){
				slot = (RDFProperty) itc.next();
				range = slot.getAllowedClses();
				if(range.contains(arg2.elementAt(b))
						/*||(slot.getAllowedClses().retainAll(typesArgB.elementAt(b).getSuperclasses(true)))*/){
				
					return true;
				}
				//recherche dans les super classes du type de l'argument
				supCls = arg2.elementAt(b).getSuperclasses(true);
				
				for(Iterator<RDFSClass> it=supCls.iterator();it.hasNext();){
					
					if(range.contains(it.next()))
						return true;
				}
				
			}//for itc	
		}//for h
	}//for b
		return false;
}
boolean isUpwardLink (RDFProperty link){
	Vector<RDFProperty> slots= new Vector<RDFProperty>();
	slots.addElement(link);
	slots.addAll((link.getSuperproperties(true)));
	
	for(int j=0; j<slots.size();j++)
	for(int i=0; i<upwardLinks.length; i++){
		if(upwardLinks[i].equals(slots.elementAt(j).getLocalName()))
			return true;
	}
	return false;
}
boolean isDownwardLink (RDFProperty link){
	Vector<RDFProperty> slots= new Vector<RDFProperty>();
	slots.addElement(link);
	slots.addAll((link.getSuperproperties(true)));
	
	for(int j=0; j<slots.size();j++)
	for(int i=0; i<downwardLinks.length; i++){
		if(downwardLinks[i].equals(slots.elementAt(j).getLocalName()))
			return true;
	}
	return false;
}
boolean isHorizontalLink (RDFProperty link){
	Vector<RDFProperty> slots= new Vector<RDFProperty>();
	slots.addElement(link);
	slots.addAll((link.getSuperproperties(true)));
	
	for(int j=0; j<slots.size();j++)
	for(int i=0; i<hrztLinks.length; i++){
		if(hrztLinks[i].equals(slots.elementAt(j).getLocalName()))
			return true;
	}
	return false;
}

int withHrztIPDirection(Vector<RDFSClass> typesArgH, Vector<RDFSClass> typesArgB, OWLModel owlModel){

	//Vector<RDFSClass> arg1 = typesArgH, arg2 = typesArgB;
	RDFProperty slot;
	
	Collection c ;
	Collection<RDFSClass> range, supCls;//range = les types possibles de l'argument
	
	//domaine = argH, range = argB
		for(int h=0; h<typesArgH.size(); h++){
			c = owlModel.getDirectTemplateSlots(typesArgH.elementAt(h));//les IP dont le domaine est le type en cours
			for(int b=0; b<typesArgB.size(); b++){
			for(Iterator<Slot> itc = c.iterator(); itc.hasNext();){
				slot = (RDFProperty) itc.next();
				range = slot.getAllowedClses();
				for(Iterator it = range.iterator(); it.hasNext();){
					if(((RDFSClass)it.next()).equals(typesArgB.elementAt(b))){
						
						if(isHorizontalLink(slot)) return 1;
						
						else return 0;
						
					}
				}
				
				//recherche dans les super classes du type de l'argument
				supCls = typesArgB.elementAt(b).getSuperclasses(true);
				RDFSClass supcls;
				for(Iterator<RDFSClass> it2=supCls.iterator();it2.hasNext();){
					supcls = it2.next();
					for(Iterator it = range.iterator(); it.hasNext();){
						if(((RDFSClass)it.next()).equals(supcls)){
							if(isHorizontalLink(slot)) return 1;
							
							else return 0;
						}
					}
					/*if(range.contains(it.next()))
						return true;*/
				}
				
			}//for itc	
		}//for h
	}//for b
		
		
		//domaine = argB, range = argH
				for(int b1=0; b1<typesArgB.size(); b1++){
					c = owlModel.getDirectTemplateSlots(typesArgB.elementAt(b1));//les IP dont le domaine est le type en cours
					for(int h1=0; h1<typesArgH.size(); h1++){
					for(Iterator<Slot> itc = c.iterator(); itc.hasNext();){
						slot = (RDFProperty) itc.next();
						range = slot.getAllowedClses();
						for(Iterator it = range.iterator(); it.hasNext();){
							if(((RDFSClass)it.next()).equals(typesArgH.elementAt(h1))){
								if(isHorizontalLink(slot)) return 1;
								
								else return 0;
							}
						}
						
						//recherche dans les super classes du type de l'argument
						supCls = typesArgH.elementAt(h1).getSuperclasses(true);
						RDFSClass supcls;
						for(Iterator<RDFSClass> it2=supCls.iterator();it2.hasNext();){
							supcls = it2.next();
							for(Iterator it = range.iterator(); it.hasNext();){
								if(((RDFSClass)it.next()).equals(supcls)){
									if(isHorizontalLink(slot)) return 1;
									
									else return 0;
								}
							}
						}
						
					}//for itc	
				}//for b1
			}//for h1
			
		return 0;//pas d'IPs
}


@SuppressWarnings("deprecation")
int anyIs_aRelationWithDirection(Vector<RDFSClass> typesArgH, Vector<RDFSClass> typesArgB, OWLModel owlModel){
reason="";
	//Vector<RDFSClass> arg1 = typesArgH, arg2 = typesArgB;
	
	
	Collection c ;
	Collection<RDFSClass> range, supCls;//range = les types possibles de l'argument
	RDFSClass cl;
	//domaine = argH, range = argB
		for(int h=0; h<typesArgH.size(); h++){
			reason = reason +"\n"+"-head: "+typesArgH.elementAt(h).getBrowserText();
			
			for(int b=0; b<typesArgB.size(); b++){
			
					reason = reason +"\n"+"-body: "+typesArgB.elementAt(b).getBrowserText();
					
					if((typesArgH.elementAt(h)).isSubclassOf(typesArgB.elementAt(b))){//upwardlink
						
						if(process.equals("bottom-up") )
								return 4;
					}
					if((typesArgB.elementAt(b)).isSubclassOf(typesArgH.elementAt(h))){//downwardlink
						
						if(process.equals("top-down") )
								return 4;
					}
						
			
		}//for h
	}//for b
		
			
		return 0;
}


@SuppressWarnings("deprecation")
int anyIPsWithDirection(Vector<RDFSClass> typesArgH, Vector<RDFSClass> typesArgB, OWLModel owlModel){
reason="";
	//Vector<RDFSClass> arg1 = typesArgH, arg2 = typesArgB;
	RDFProperty slot;
	
	Collection c ;
	Collection<RDFSClass> range, supCls,supCls1;//range = les types possibles de l'argument
	RDFSClass cl;
	RDFSClass supcls, cls;
	//domaine = argH, range = argB
		for(int h=0; h<typesArgH.size(); h++){
			reason = reason +"\n"+"-head: "+typesArgH.elementAt(h).getBrowserText();
			c = owlModel.getDirectTemplateSlots(typesArgH.elementAt(h));//les IP dont le domaine est le type en cours
			for(int b=0; b<typesArgB.size(); b++){
			for(Iterator<Slot> itc = c.iterator(); itc.hasNext();){
				slot = (RDFProperty) itc.next();
				range = slot.getAllowedClses();
				for(Iterator it = range.iterator(); it.hasNext();){
					reason = reason +"\n"+"-body: "+typesArgB.elementAt(b).getBrowserText();
					cl=(RDFSClass)it.next();
					if((cl).equals(typesArgB.elementAt(b))){
						
						if((process.equals("bottom-up") && isUpwardLink(slot))
							|| (process.equals("top-down") && isDownwardLink(slot))
								){
							
							reason = reason +"\n"+"anyIpsWith direction1: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgH.elementAt(h).getBrowserText()+"-"+cl.getBrowserText()+"\n";
						return 12;}
					}
				}
				
				//recherche dans les super classes du type de l'argument B
				supCls = typesArgB.elementAt(b).getSuperclasses(true);
				
				for(Iterator<RDFSClass> it2=supCls.iterator();it2.hasNext();){
					supcls = it2.next();
					for(Iterator it = range.iterator(); it.hasNext();){
						cl=(RDFSClass)it.next();
						if((cl).equals(supcls)){
							if((process.equals("bottom-up") && isUpwardLink(slot))
									|| (process.equals("top-down") && isDownwardLink(slot))
										){
								reason = reason +"\n"+"anyIpsWith direction2: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgH.elementAt(h).getBrowserText()+"-"+cl.getBrowserText()+"\n";
							return 10;///8;
							}
						}
					}
					
				}
				
			}//for itc	
		}//for h
	}//for b
		
		
		//domaine = argB, range = argH
				for(int b1=0; b1<typesArgB.size(); b1++){
					reason = reason +"\n"+"-body: "+typesArgB.elementAt(b1).getBrowserText();
					//les IP dont le domaine est le type en cours
					c = owlModel.getDirectTemplateSlots(typesArgB.elementAt(b1));
					
					for(int h1=0; h1<typesArgH.size(); h1++){
						reason = reason +"\n"+"-head: "+typesArgH.elementAt(h1).getBrowserText();
					for(Iterator<Slot> itc = c.iterator(); itc.hasNext();){
						slot = (RDFProperty) itc.next();
						range = slot.getAllowedClses();
						for(Iterator it = range.iterator(); it.hasNext();){
							cl=(RDFSClass)it.next();
							if((cl).equals(typesArgH.elementAt(h1))){
								if((process.equals("top-down") && isUpwardLink(slot))
									|| (process.equals("bottom-up") && isDownwardLink(slot))
										){
									reason = reason +"\n"+"anyIpsWith direction3: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgB.elementAt(b1).getBrowserText()+"-"+cl.getBrowserText()+"\n";
								return 11;//10;
								}
							}
						}
						
						//recherche dans les super classes du type de l'argument h
						supCls = typesArgH.elementAt(h1).getSuperclasses(true);
						
						for(Iterator<RDFSClass> it2=supCls.iterator();it2.hasNext();){
							supcls = it2.next();
							for(Iterator it = range.iterator(); it.hasNext();){
								cls=(RDFSClass)it.next();
								if((cls).equals(supcls)){
									if((process.equals("top-down") && isUpwardLink(slot))
											|| (process.equals("bottom-up") && isDownwardLink(slot))
												){
										reason = reason +"\n"+"anyIpsWith direction4: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgB.elementAt(b1).getBrowserText()+"-"+cls.getBrowserText()+"\n";
									return 9;
									}
								}
							}
						}
						
					}//for itc	
				}//for h1
			}//for b1
	
				
				//domaine =SUP(argH), range = argB 
					RDFSClass ch;
						for(int h=0; h<typesArgH.size(); h++){
							supCls1= typesArgH.elementAt(h).getSuperclasses(true);
							for(Iterator<RDFSClass> ith = supCls1.iterator(); ith.hasNext();){
								ch=ith.next();
							
							reason = reason +"\n"+"-head: "+ch.getBrowserText();
							c = owlModel.getDirectTemplateSlots(ch);//les IP dont le domaine est le type en cours
							for(int b=0; b<typesArgB.size(); b++){
							for(Iterator<Slot> itc = c.iterator(); itc.hasNext();){
								slot = (RDFProperty) itc.next();
								//System.out.println("@@@@@@@@@@@@@@@@@---"+ch.getBrowserText()+"-----"+slot.getBrowserText()+"------"+slot.getClass().getName()+"--");;
								if(slot.getClass().getName().equals("edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty")){
								range = slot.getRanges(true);
								
								for(Iterator<RDFSClass> it = range.iterator(); it.hasNext();){
									reason = reason +"\n"+"-body: "+typesArgB.elementAt(b).getBrowserText();
									cl=it.next();
									if((cl).equals(typesArgB.elementAt(b))){
										
										if((process.equals("bottom-up") && isUpwardLink(slot))
											|| (process.equals("top-down") && isDownwardLink(slot))
												){
											
											reason = reason +"\n"+"anyIpsWith direction1: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgH.elementAt(h).getBrowserText()+"-"+cl.getBrowserText()+"\n";
										return 8;}
									}
								}
								
								//recherche dans les super classes du type de l'argument B
								//domaine =SUP(argH), range = SUP(argB)
								supCls = typesArgB.elementAt(b).getSuperclasses(true);
								
								for(Iterator<RDFSClass> it2=supCls.iterator();it2.hasNext();){
									supcls = it2.next();
									for(Iterator it = range.iterator(); it.hasNext();){
										cl=(RDFSClass) it.next();
										if((cl).equals(supcls)){
											if((process.equals("bottom-up") && isUpwardLink(slot))
													|| (process.equals("top-down") && isDownwardLink(slot))
														){
												reason = reason +"\n"+"anyIpsWith direction2: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgH.elementAt(h).getBrowserText()+"-"+cl.getBrowserText()+"\n";
											return 6;///8;
											}
										}
									}
									
								}
							}//if individual property	
							}//for itc	
						}//for b
							}//for ith
					}//for h
						
					
				
				
						//domaine = Sup(argB), range = argH 
						for(int b1=0; b1<typesArgB.size(); b1++){
							supCls1= typesArgB.elementAt(b1).getSuperclasses(true);
							for(Iterator<RDFSClass> itb = supCls1.iterator(); itb.hasNext();){
								ch=itb.next();
							
							reason = reason +"\n"+"-body: "+ch.getBrowserText();
							//les IP dont le domaine est le type en cours
							c = owlModel.getDirectTemplateSlots(ch);
							
							for(int h1=0; h1<typesArgH.size(); h1++){
								reason = reason +"\n"+"-head: "+typesArgH.elementAt(h1).getBrowserText();
							for(Iterator<Slot> itc = c.iterator(); itc.hasNext();){
								slot = (RDFProperty) itc.next();
								if(slot.getClass().getName().equals("edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty")){
								range = slot.getRanges(true);
								for(Iterator it = range.iterator(); it.hasNext();){
									cl=(RDFSClass)it.next();
									if((cl).equals(typesArgH.elementAt(h1))){
										if((process.equals("top-down") && isUpwardLink(slot))
											|| (process.equals("bottom-up") && isDownwardLink(slot))
												){
											reason = reason +"\n"+"anyIpsWith direction3: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgB.elementAt(b1).getBrowserText()+"-"+cl.getBrowserText()+"\n";
										return 7;//10;
										}
									}
								}
								
								//domaine = Sup(argB), range = Sup(argH)
								//recherche dans les super classes du type de l'argument H
								supCls = typesArgH.elementAt(h1).getSuperclasses(true);
								
								for(Iterator<RDFSClass> it2=supCls.iterator();it2.hasNext();){
									supcls = it2.next();
									for(Iterator it = range.iterator(); it.hasNext();){
										cls=(RDFSClass)it.next();
										if((cls).equals(supcls)){
											if((process.equals("top-down") && isUpwardLink(slot))
													|| (process.equals("bottom-up") && isDownwardLink(slot))
														){
												reason = reason +"\n"+"anyIpsWith direction4: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgB.elementAt(b1).getBrowserText()+"-"+cls.getBrowserText()+"\n";
											return 5;
											}
										}
									}
								}
								}//if individual property
							}//for itc	
						}//for h1
							}//for itb
					}//for b1
						
				
				
				
				
				
				
				
		return 0;
}
int anyIPsWithDirectionOriginal(Vector<RDFSClass> typesArgH, Vector<RDFSClass> typesArgB, OWLModel owlModel){
reason="";
	//Vector<RDFSClass> arg1 = typesArgH, arg2 = typesArgB;
	RDFProperty slot;
	
	Collection c ;
	Collection<RDFSClass> range, supCls;//range = les types possibles de l'argument
	RDFSClass cl;
	//domaine = argH, range = argB
		for(int h=0; h<typesArgH.size(); h++){
			reason = reason +"\n"+"-head: "+typesArgH.elementAt(h).getBrowserText();
			c = owlModel.getDirectTemplateSlots(typesArgH.elementAt(h));//les IP dont le domaine est le type en cours
			for(int b=0; b<typesArgB.size(); b++){
			for(Iterator<Slot> itc = c.iterator(); itc.hasNext();){
				slot = (RDFProperty) itc.next();
				range = slot.getAllowedClses();
				for(Iterator it = range.iterator(); it.hasNext();){
					reason = reason +"\n"+"-body: "+typesArgB.elementAt(b).getBrowserText();
					cl=(RDFSClass)it.next();
					if((cl).equals(typesArgB.elementAt(b))){
						
						if((process.equals("bottom-up") && isUpwardLink(slot))
							|| (process.equals("top-down") && isDownwardLink(slot))
								){
							
							reason = reason +"\n"+"anyIpsWith direction1: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgH.elementAt(h).getBrowserText()+"-"+cl.getBrowserText()+"\n";
						return 12;}
					}
				}
				
				//recherche dans les super classes du type de l'argument B
				supCls = typesArgB.elementAt(b).getSuperclasses(true);
				RDFSClass supcls;
				for(Iterator<RDFSClass> it2=supCls.iterator();it2.hasNext();){
					supcls = it2.next();
					for(Iterator it = range.iterator(); it.hasNext();){
						cl=(RDFSClass)it.next();
						if((cl).equals(supcls)){
							if((process.equals("bottom-up") && isUpwardLink(slot))
									|| (process.equals("top-down") && isDownwardLink(slot))
										){
								reason = reason +"\n"+"anyIpsWith direction2: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgH.elementAt(h).getBrowserText()+"-"+cl.getBrowserText()+"\n";
							return 10;///8;
							}
						}
					}
					/*if(range.contains(it.next()))
						return true;*/
				}
				
			}//for itc	
		}//for h
	}//for b
		
		
		//domaine = argB, range = argH
				for(int b1=0; b1<typesArgB.size(); b1++){
					reason = reason +"\n"+"-body: "+typesArgB.elementAt(b1).getBrowserText();
					//les IP dont le domaine est le type en cours
					c = owlModel.getDirectTemplateSlots(typesArgB.elementAt(b1));
					
					for(int h1=0; h1<typesArgH.size(); h1++){
						reason = reason +"\n"+"-head: "+typesArgH.elementAt(h1).getBrowserText();
					for(Iterator<Slot> itc = c.iterator(); itc.hasNext();){
						slot = (RDFProperty) itc.next();
						range = slot.getAllowedClses();
						for(Iterator it = range.iterator(); it.hasNext();){
							cl=(RDFSClass)it.next();
							if((cl).equals(typesArgH.elementAt(h1))){
								if((process.equals("top-down") && isUpwardLink(slot))
									|| (process.equals("bottom-up") && isDownwardLink(slot))
										){
									reason = reason +"\n"+"anyIpsWith direction3: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgB.elementAt(b1).getBrowserText()+"-"+cl.getBrowserText()+"\n";
								return 8;//10;
								}
							}
						}
						
						//recherche dans les super classes du type de l'argument
						supCls = typesArgH.elementAt(h1).getSuperclasses(true);
						RDFSClass supcls,cls;
						for(Iterator<RDFSClass> it2=supCls.iterator();it2.hasNext();){
							supcls = it2.next();
							for(Iterator it = range.iterator(); it.hasNext();){
								cls=(RDFSClass)it.next();
								if((cls).equals(supcls)){
									if((process.equals("top-down") && isUpwardLink(slot))
											|| (process.equals("bottom-up") && isDownwardLink(slot))
												){
										reason = reason +"\n"+"anyIpsWith direction4: "+slot.getLocalName()+"("+slot.getSuperpropertyCount()+"):"+typesArgB.elementAt(b1).getBrowserText()+"-"+cls.getBrowserText()+"\n";
									return 6;
									}
								}
							}
						}
						
					}//for itc	
				}//for h1
			}//for b1
			
		return 0;
}

void insertWithoutRedendency(Vector<RDFSClass> vect, Collection<RDFSClass> types )
{
	
	if(vect.isEmpty()){
		vect.addAll(types);
	}
	else
	{
	RDFSClass t;
	for(int i=0; i<types.size() ;i++)
	{
			for(Iterator<RDFSClass> it1= types.iterator(); it1.hasNext();)
			{
				t=(RDFSClass)it1.next();
				if(!vect.contains(t)){
					vect.add(t);	
				}
		}
	}
	
}
	
}

DirectedGraph getDirectedGraph(){
	DirectedGraph graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class); 

	for(int i=0; i<size; i++){
		graph.addVertex(label[i]);
	}
	for(int i=0; i<size; i++)
		for(int j=0; j<size; j++){
			if(myMatrix[i][j]!=0){
				graph.addEdge(label[i], label[j]);
			}
		}
	return graph;
}
List getHawickJamesSimpleCycles(int[][] matrix){
	

	DirectedGraph graph = getDirectedGraph(); 

	
	System.out.println("extraction des  HawickJamesSimple cycles...");
	//extraction des cycles
	HawickJamesSimpleCycles  	cd = new HawickJamesSimpleCycles((DirectedGraph) graph);
	//List cycles= cd.findSimpleCycles();
	//cd = new HawickJamesSimpleCycles((DirectedGraph) graph);
	List cycles= cd.findSimpleCycles();
	
	
	
	return cycles;
	
}

	/*
	 * List getCycles(int[][] matrix){
	 * 
	 * //ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(BmyMatrix, label);
	 * boolean[][] BmyMatrix = new boolean[size][size];
	 * 
	 * for(int i=0; i<size; i++){
	 * 
	 * for(int j=0; j<size; j++){
	 * 
	 * if(matrix[i][j]!=0) BmyMatrix[i][j]=true; else BmyMatrix[i][j]=false; } }
	 * System.out.println("extraction des cycles..."); //extraction des cycles
	 * ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(BmyMatrix, label);
	 * 
	 * return ecs.getElementaryCycles();
	 * 
	 * }
	 */
int getIndexInMyMatrix(Rule r){
	String rTxt = r.getRuleText();
	for(int i=0; i<size; i++){
		if(rTxt.equals(rulesTab[i].getBrowserText()))
			return i;		
	}
	return -1;
}
boolean connected =false;

boolean isConnectedTo(int r1, int r2){
	// stockage du chemin pendant l'exploration recursive
	path = new int[size];
	 
	// verrou pour la recherche taboo (tous initialisés à "false" par défaut)
	 taboo = new boolean[size];
	 
	// sommets de départ/arrivé souhaités
	int source=r1;
	int target=r2;

	explore(source,target,0);
	
    if(connected) return true;
    return false;
	
}



void explore(int position, int target,int depth) {
	path[depth]=position;
 
	// on est sur le sommet d'arrivé -> fini
	if (position==target) {
		// affiche la solution
		for(int i=0;i<=depth;i++) System.out.print(path[i]+" ");
		System.out.print("\n");
		connected=true;
		return;
	}
 
	// sinon...
 
	taboo[position]=true; // on pose un caillou
 
	// on explore les chemins restants
	for(int i=0;i<size && !connected;i++) {
		if (myMatrix[position][i]==0 || taboo[i]) continue;
		explore(i,target,depth+1);
	}
 
	taboo[position]=false; // on retire le caillou
}

String reason="";
	String process;
	int size ;
	public boolean cycle = false;
	int [][] myMatrix;
	int [][] myMatrix8;
	int [][] myMatrix8hb;
	int [][] myMatrix8hh;
	int [][] myMatrix11;
	int[][] grpsGrph ;
	int [][] myMatrixHrzt;
	int[][] groupGraph;
	//int [][] myMatrixAlg4;
	//int [][] myMatrixAlg5;
	//int [][] myMatrixAlg6;
	int [][] matrix;
	String [] label ;
	SWRLImp [] rulesTab;
	ArrayList [][]edgeProp;
	String[] hrztLinks = {
			//SPM
			"hasADiffProperty", "hasAnalogComponent","isDiffFrom",
			"isIdenticalOrEquivalentTo","isLargerThan","isStrongerThan","isSmallerThan",
			"isWeakerThan", "hasTechDiffWith"
			//ontoFood
			,
			"diet_breakfast_bmi20gi60",
			"diet_breakfast_bmi30gi120","diet_dinner_bmi20gi60","diet_dinner_bmi30gi120",
			"diet_dinner_general","diet_lunch_bmi20gi60","diet_lunch_bmi30gi120","diet_lunch_general",
			"diet_snack_bmi20gi60","diet_snack_bmi30gi120","diet_snack_general_aft",
			"diet_snack_general_morn","has_breakfast","has_dinner","has_disease",
			"has_lunch"
			,};
	String[] upwardLinks = {
			//SPM
			"isMoreSpecificThan", "hasSourceComponent" 
			,};
	String[] downwardLinks = {
			//SPM
			"isMoreGeneralThan", "hasDestinationComponent",
			"hasAssertion","hasAlternative","hasProperty"
			//OntoFood
			,"contain","has_name","has_meal_level"
			,};
	
	public List createGraphVerticeData(OWLModel owlModel) throws OntologyLoadException {
	
	
	inFile("D:/c.txt", "",false);
	inFile("D:/fp.txt", "",false);
		
	    SWRLFactory factory = new SWRLFactory(owlModel);
	    RDFProperty  bph = owlModel.getRDFProperty("http://swrl.stanford.edu/ontologies/3.3/swrla.owl#hasBuiltInPhrase");
	    Collection rules = factory.getImps();
	    SWRLImp element;
	    
	    //paraphraseRule para = new paraphraseRule();
	    process = "top-down";/*"bottom-up";*///ou 
	    size = rules.size();
	    matrix = new int[ size ][ size ];
	    myMatrix = new int[ size ][ size ];
	    myMatrix8 = new int[ size ][ size ];
	    myMatrix8hb = new int[ size ][ size ];
	    myMatrix8hh = new int[ size ][ size ];
	    myMatrix11 = new int[ size ][ size ];
	    myMatrixHrzt = new int[ size ][ size ];
	   
	    int [][] matrixExact = new int[ size ][ size ];
	    ArrayList head[] = new ArrayList [ size ];
	    ArrayList body[] = new ArrayList [ size ];
	    Object[] rulesList = rules.toArray();
	    label = new String[size];
	    rulesTab = new SWRLImp [size];
	    edgeProp= new ArrayList[ size ][ size ];
	    
	    int counter = 0;
	 
	    List vertexList = new ArrayList(size);
	    final Random random = new Random();
	    System.out.println("**************************---- "+owlModel.getName()+" ----*******************************************");
	    Vector<Vector<RDFSClass>> bodyCls = new Vector<Vector<RDFSClass>>();
    	Vector<Vector<RDFSClass>> headCls = new Vector<Vector<RDFSClass>>();
	    for(int i=0; i<size; i++){
	    	bodyCls.add(new Vector<RDFSClass>());
	    	headCls.add(new Vector<RDFSClass>());
	    	for(int j=0; j<size; j++)
	    	{
	    		myMatrix[i][j] = 0;
	    		edgeProp[i][j] = new ArrayList();
	    	}
	    }
///////////////
//////////////	    
	    
	    for (Iterator it=rules.iterator(); it.hasNext(); )
	    {
	    	element = (SWRLImp) it.next();
	    	rulesTab[counter]=element;
	    	head[ counter ] = (ArrayList) element.getHead().getValues();
	    	body[ counter ] = (ArrayList) element.getBody().getValues();
	    	label[ counter ] = element.getLocalName();
	    	
	    	counter++;
	    }
	    //////Axiome///////////////
	    
    	SWRLAtom atom,Natom;
    	RDFSClass ruleType;
    	RDFSClass NruleType;
    	String ruleTypeName,NruleTypeName,S,NS;
    	int tmp;
    	RDFResource aa,bb;
	    
	    for(int i=0; i<size; i++)
	    {
	    	for (Iterator it=head[i].iterator(); it.hasNext(); )
		    {
	    		atom = (SWRLAtom) it.next();
	    		ruleType = atom.getRDFType();
	    		ruleTypeName = ruleType.getBrowserText();
	    		
	    		if(ruleTypeName.equals("swrl:ClassAtom"))
	    	    {
	    			S = ((((SWRLClassAtom) atom).getClassPredicate()).getBrowserText());
	    			aa = (RDFResource) ((SWRLClassAtom) atom).getArgument1();
	    			
	    			for(int j=0; j<size; j++)
	    			{
	    				for (Iterator Nit=body[j].iterator(); Nit.hasNext(); )
	    			    {
	    		    		Natom = (SWRLAtom) Nit.next();
	    		    		NruleType = Natom.getRDFType();
	    		    		NruleTypeName = NruleType.getBrowserText();
	    		    		
	    		    		if(NruleTypeName.equals("swrl:ClassAtom"))
	    		    	    {
	    		    			NS = ((((SWRLClassAtom) Natom).getClassPredicate()).getBrowserText());
	    		    			bb = (RDFResource) ((SWRLClassAtom) Natom).getArgument1();
	    		    			
	    		    			if(S.equals(NS) && (i!=j || aa.getBrowserText() != bb.getBrowserText()))
	    		    				matrix[i][j]++;
	    		    				edgeProp[i][j].add(2);
	    		    	    }
	    			    }
	    			}
	    	    }
	    		
	    		if(ruleTypeName.equals("swrl:IndividualPropertyAtom"))
	    	    {
	    			S = ((((SWRLIndividualPropertyAtom) atom).getPropertyPredicate()).getBrowserText());
	    			aa = (RDFResource) ((SWRLIndividualPropertyAtom) atom).getArgument1();
	    			
	    			for(int j=0; j<size; j++)
	    			{
	    				for (Iterator Nit=body[j].iterator(); Nit.hasNext(); )
	    			    {
	    		    		Natom = (SWRLAtom) Nit.next();
	    		    		NruleType = Natom.getRDFType();
	    		    		NruleTypeName = NruleType.getBrowserText();
	    		    		
	    		    		if(NruleTypeName.equals("swrl:IndividualPropertyAtom"))
	    		    	    {
	    		    			NS = ((((SWRLIndividualPropertyAtom) Natom).getPropertyPredicate()).getBrowserText());
	    		    			bb = (RDFResource) ((SWRLIndividualPropertyAtom) Natom).getArgument1();
	    		    			
	    		    			if(S.equals(NS) && (i!=j || aa.getBrowserText() != bb.getBrowserText()))
	    		    			{
	    		    				matrix[i][j]++;
	    		    				edgeProp[i][j].add(3);
	    		    				//System.out.println("S = " + S);
	    		    			}
	    		    	    }
	    			    }
	    			}
	    	    }
	    		
	    		if(ruleTypeName.equals("swrl:DatavaluedPropertyAtom"))
	    	    {
	    			S = ((((SWRLDatavaluedPropertyAtom) atom).getPropertyPredicate()).getBrowserText());
	    			aa = (RDFResource) ((SWRLDatavaluedPropertyAtom) atom).getArgument1();
	    			
	    			for(int j=0; j<size; j++)
	    			{
	    				for (Iterator Nit=body[j].iterator(); Nit.hasNext(); )
	    			    {
	    		    		Natom = (SWRLAtom) Nit.next();
	    		    		NruleType = Natom.getRDFType();
	    		    		NruleTypeName = NruleType.getBrowserText();
	    		    		
	    		    		if(NruleTypeName.equals("swrl:DatavaluedPropertyAtom"))
	    		    	    {
	    		    			NS = ((((SWRLDatavaluedPropertyAtom) Natom).getPropertyPredicate()).getBrowserText());
	    		    			bb = (RDFResource) ((SWRLDatavaluedPropertyAtom) Natom).getArgument1();
	    		    			
	    		    			if(S.equals(NS) && (i!=j || aa.getBrowserText() == bb.getBrowserText()))
	    		    			{
	    		    				matrix[i][j]++;
	    		    				edgeProp[i][j].add(4);
	    		    				//System.out.println("N = " + S);
	    		    			}
	    		    	    }
	    			    }
	    			}
	    	    }
	    		
		    }
	    }
	    int tot=0;   
	  for(int i=0;i<size;i++){
		  for(int j=0; j<size; j++){
///////////////////////////numéros de R1 et R2
	String R2name = label[i];
		int nr2 = 200;
		try{
			nr2 = Integer.parseInt(R2name.substring(5,R2name.length()));
			//System.out.println("nr2="+nr2);
		}catch(Exception ex){}
		String R1name = label[j];
		int nr1 = 200;
		try{
			nr1 = Integer.parseInt(R1name.substring(5,R1name.length()));
			//System.out.println("nr1="+nr1);
			
		}catch(Exception ex){}
		////////////////////////////////////
		
		 if(
				 ((nr2==4)&&(nr1>4 && nr1<8))||	 
		 	    	((nr2>4 && nr2<8)&&(nr1>7 && nr1<24))||
		 	    	((nr2>-1 && nr2<4)&&(nr1>7 && nr1<24))
		 	    	
		 	    	
 				  ){
  	    	tot++;//total des dépendances correctes qui doivent être trouvées (dep exactes)
  	    	matrixExact[i][j]++;
  	    }
      		
		  }
	  }
	  
	  
	  /*Résultat Axiome*/
  	int c=0, fp=0;
  	for(int i=0; i<size; i++)
  		for(int j=0; j<size; j++){
  			if(matrixExact[i][j]!=0 && matrix[i][j]!=0 ){
  				inFileln("D:/c.txt","C: "+label[i]+"->"+label[j],true);
  			c++;
  			}
  			else
  			
  				{if(matrixExact[i][j]== 0 && matrix[i][j]!=0 )
  					if(
	    					   !(
 							   		   ((getRuleNum(label[i])>4&&getRuleNum(label[i])<8) &&
 	    								(getRuleNum(label[j])>4&&getRuleNum(label[j])<8))||
 	    		    				   ((getRuleNum(label[i])>-1&&getRuleNum(label[i])<4) && 
 	    		    				    (getRuleNum(label[j])>-1&&getRuleNum(label[j])<4))||	
 	    		    				   ((getRuleNum(label[i])>7&&getRuleNum(label[i])<24)&&
 	    		    				    (getRuleNum(label[j])>7&&getRuleNum(label[j])<24)))


  						  
	    		    				   ){
  					inFileln("D:/fp.txt", "f+: "+label[i]+"->"+label[j], true);	
  					fp++;
  					}
  				}
  				
  		}
  	System.out.println("******Axiomé******");
  	System.out.println("dépendances correctes : " + c + "/"+tot);
  	System.out.println("Faux positifs : " + fp);
  	
	  
	  
	  
	    //////////////////////////////////// alg3
	    SWRLAtom atomH, atomB;
	    RDFSClass typeH, typeB;
	    String typeHName, typeBName, predicateHName, predicateHType, predicateBName,predicateBType;
	    RDFResource  predH, predB;
	    RDFResource arg1H, arg2H, arg1B, arg2B;
	    Vector<RDFSClass> typesArg1H;
		Vector<RDFSClass> typesArg2H;
		Vector<RDFSClass> typesArg1B;
		Vector<RDFSClass> typesArg2B;
	    
	   // int tmp;
	   //total des dépendances exactes
	    boolean trouve=false;
	    for(int i=0; i<size; i++){//for R2	
    		for(int j=0; j<size; j++){//for R1
    			reason=label[i]+"-->"+label[j]+"\n";
    			if(i==j)
    				myMatrix[i][j]=0;
    			else{
    				
    			for (Iterator itH=head[i].iterator(); itH.hasNext(); )
    		    {
    	    		atomH = (SWRLAtom) itH.next();
    	    		typeH = atomH.getRDFType();
    	    		typeHName = typeH.getBrowserText();
    	    		//System.out.println(label[i]);
    	    		for (Iterator itB=body[j].iterator(); itB.hasNext(); )
        		    {
    	    			//System.out.println(label[i]+"-->"+label[j]);
        	    		atomB = (SWRLAtom) itB.next();
        	    		typeB = atomB.getRDFType();
        	    		typeBName = typeB.getBrowserText();
        	    		
        	    		if(typeHName.equals("swrl:ClassAtom"))/*TODO cls*/
        	    	    {
        	    			predH = (((SWRLClassAtom) atomH).getClassPredicate());
        	    			//arg1H = (RDFResource) ((SWRLClassAtom) atomH).getArgument1();
        	    			typesArg1H = getVarType(label[i],/*(SWRLVariable)*/((SWRLClassAtom) atomH).getArgument1() , body[i], head[i], "head", owlModel);
        	    			insertWithoutRedendency(headCls.elementAt(i), typesArg1H);
        	    			if(typeBName.equals("swrl:ClassAtom")){//cls/cls
        	    				predB = (((SWRLClassAtom) atomB).getClassPredicate());
        	    				typesArg1B = getVarType(label[j],/*(SWRLVariable)*/((SWRLClassAtom) atomB).getArgument1() , body[j], head[j], "body", owlModel);
        	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
        	    				if(equalOrEquivalent((RDFSClass)predH, (RDFSClass)predB)){
	    		    				myMatrix[i][j]=Math.max(myMatrix[i][j], 100);
	    		    				edgeProp[i][j].add(2);
	    		    				reason=reason+"cls/cls"+"\n";
	    		    			}else
	    		    				if(equalOrEquivalent((RDFSClass)predH, (RDFSClass)predB)){
		    		    				myMatrix[i][j]=Math.max(myMatrix[i][j], 89);
		    		    				edgeProp[i][j].add(2);
		    		    				reason=reason+"cls/cls"+"\n";
		    		    			}
	    		    				
        	    			}else
        	    			/*TODO CLS/IP*/
        	    			if(typeBName.equals("swrl:IndividualPropertyAtom"))
            	    	    {
    	    	    			predB = ((SWRLIndividualPropertyAtom) atomB).getPropertyPredicate();
    	    	    			arg1B = (RDFResource) ((SWRLIndividualPropertyAtom) atomB).getArgument1();
            	    			arg2B = (RDFResource) ((SWRLIndividualPropertyAtom) atomB).getArgument2();
            	    			typesArg1B = getVarType(label[j],/*(SWRLVariable)*/arg1B, body[j], head[j], "body", owlModel);
	    	    				typesArg2B = getVarType(label[j],/*(SWRLVariable)*/arg2B, body[j], head[j], "body", owlModel);
	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg2B);
    	    	    					if(dependsOn(typesArg1H, typesArg1B)||dependsOn(typesArg1H, typesArg2B)){
    	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 26);
	    		    				edgeProp[i][j].add(3);	
	    		    				reason=reason+"cls/ip"+"\n";
    	    	    			}
            	    	    }else
            	    	    	if(typeBName.equals("swrl:DifferentIndividualsAtom"))
                	    	    {
        	    	    			//predB = ((SWRLIndividualPropertyAtom) atomB).getPropertyPredicate();
        	    	    			arg1B = (RDFResource) ((SWRLDifferentIndividualsAtom) atomB).getArgument1();
                	    			arg2B = (RDFResource) ((SWRLDifferentIndividualsAtom) atomB).getArgument2();
                	    			typesArg1B = getVarType(label[j],/*(SWRLVariable)*/arg1B, body[j], head[j], "body", owlModel);
    	    	    				typesArg2B = getVarType(label[j],/*(SWRLVariable)*/arg2B, body[j], head[j], "body", owlModel);
    	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
    	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg2B);
        	    	    			
        	    	    					if(dependsOn(typesArg1H, typesArg1B)||dependsOn(typesArg1H, typesArg2B)){
        	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 25);
    	    		    				edgeProp[i][j].add(3);	
    	    		    				reason=reason+"cls/diffrentfrom"+"\n";
        	    	    			}
                	    	    }
    	    	    		else
            	    	    	if(typeBName.equals("swrl:SameIndividualAtom"))
                	    	    {
        	    	    			//predB = ((SWRLIndividualPropertyAtom) atomB).getPropertyPredicate();
        	    	    			arg1B = (RDFResource) ((SWRLSameIndividualAtom) atomB).getArgument1();
                	    			arg2B = (RDFResource) ((SWRLSameIndividualAtom) atomB).getArgument2();
                	    			typesArg1B = getVarType(label[j],/*(SWRLVariable)*/arg1B, body[j], head[j], "body", owlModel);
    	    	    				typesArg2B = getVarType(label[j],/*(SWRLVariable)*/arg2B, body[j], head[j], "body", owlModel);
    	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
    	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg2B);
        	    	    			
        	    	    					if(dependsOn(typesArg1H, typesArg1B)||dependsOn(typesArg1H, typesArg2B)){
        	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 39);
    	    		    				edgeProp[i][j].add(3);	
    	    		    				reason=reason+"cls/sameas"+"\n";
        	    	    			}
                	    	    }
            	    	    	
        	    	    }else
        	    	    	if(typeHName.equals("swrl:IndividualPropertyAtom")/*||
        	    	    	typeHName.equals("swrl:SameIndividualAtom")||
        	    	    	typeHName.equals("swrl:DifferentIndividualsAtom")*/)/*TODO ip*/
            	    	    {
        	    	    		predH = (((SWRLIndividualPropertyAtom) atomH).getPropertyPredicate());
            	    			arg1H = (RDFResource) ((SWRLIndividualPropertyAtom) atomH).getArgument1();
            	    			arg2H = (RDFResource) ((SWRLIndividualPropertyAtom) atomH).getArgument2();
            	    			//System.out.println(arg2H.getRDFType().getBrowserText());
            	    			typesArg1H = getVarType(label[i],/*(SWRLVariable)*/arg1H, body[i], head[i], "head", owlModel);
	    	    				typesArg2H = getVarType(label[i],/*(SWRLVariable)*/arg2H, body[i], head[i], "head", owlModel);
	    	    				insertWithoutRedendency(headCls.elementAt(i), typesArg1H);
	    	    				insertWithoutRedendency(headCls.elementAt(i), typesArg2H);
        	    	    		if(typeBName.equals("swrl:IndividualPropertyAtom"))//IP/IP
                	    	    {
        	    	    			predB = ((SWRLIndividualPropertyAtom) atomB).getPropertyPredicate();
        	    	    			arg1B = (RDFResource) ((SWRLIndividualPropertyAtom) atomB).getArgument1();
                	    			arg2B = (RDFResource) ((SWRLIndividualPropertyAtom) atomB).getArgument2();
                	    			typesArg1B = getVarType(label[j],/*(SWRLVariable)*/arg1B, body[j], head[j], "body", owlModel);
    	    	    				typesArg2B = getVarType(label[j],/*(SWRLVariable)*/arg2B, body[j], head[j], "body", owlModel);
    	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
    	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg2B);
    	    	    				if(equalOrEquivalentOrSubOf((DefaultOWLObjectProperty)predH, (DefaultOWLObjectProperty)predB)){
    	    	    					if(dependsOn(typesArg1H, typesArg1B)&& equalOrEquivalent(typesArg2H, typesArg2B)){
    	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 99);
    	    	    						edgeProp[i][j].add(3);	
    	    	    						reason=reason+"ip/ip"+"\n";
    	    	    					}
    	    	    				/*if(equalOrEquivalent((DefaultOWLObjectProperty)predH, (DefaultOWLObjectProperty)predB)){
    	    	    					if(equalOrEquivalent(typesArg1H, typesArg1B)&& equalOrEquivalent(typesArg2H, typesArg2B)){
    	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 99);
    	    	    						edgeProp[i][j].add(3);	
    	    	    						reason=reason+"ip/ip"+"\n";
    	    	    					}else
    	    	    			    	if(subOf(typesArg1H, typesArg1B)&& subOf(typesArg2H, typesArg2B)){
	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 88);
	    	    						edgeProp[i][j].add(3);	
	    	    						reason=reason+"ip/ip"+"\n";
    	    	    			    	}else
    	    	    			    	if(dependsOn(typesArg1H, typesArg1B)&& dependsOn(typesArg2H, typesArg2B)){
    	    	    			    	myMatrix[i][j]=Math.max(myMatrix[i][j], 86);
    	    	    			    	edgeProp[i][j].add(3);	
    	    	    			    	reason=reason+"ip/ip"+"\n";
    	    	    			    	}	
    	    	    				
    	    	    				}else
    	    	    				if(subOf((DefaultOWLObjectProperty)predH, (DefaultOWLObjectProperty)predB)){
        	    	    				if(equalOrEquivalent(typesArg1H, typesArg1B)&& equalOrEquivalent(typesArg2H, typesArg2B)){
        	    	    				myMatrix[i][j]=Math.max(myMatrix[i][j], 87);
    	    		    				edgeProp[i][j].add(3);	
    	    		    				reason=reason+"ip/ip"+"\n";
        	    	    				}else
        	    	    				if(subOf(typesArg1H, typesArg1B)&& subOf(typesArg2H, typesArg2B)){
    	    	    					myMatrix[i][j]=Math.max(myMatrix[i][j], 69);
    	    	    					edgeProp[i][j].add(3);	
    	    	    					reason=reason+"ip/ip"+"\n";
        	    	    				}else
        	    	    			    if(dependsOn(typesArg1H, typesArg1B)&& dependsOn(typesArg2H, typesArg2B)){//pour les cas où l'un des couples args (ceux ayant la mm position) présente une églité et l'autre une relation d'hérarchie
            	    	    			 myMatrix[i][j]=Math.max(myMatrix[i][j], 68);
            	    	    			 edgeProp[i][j].add(3);	
            	    	    			 reason=reason+"ip/ip"+"\n";
            	    	    			 }
            	    		
        	    	    					*/
        	    	    				else//(alg 3)ajout de la possibilité de l'existence d'une IP entre les deux types des args ayant la mm position
        	    	    				{
        	    	    					if((anyIPsWithDirection(typesArg1H,typesArg1B,owlModel)!=0||anyIPsWithDirection(typesArg1B,typesArg1H,owlModel)!=0) 
        	    	    						&& (anyIPsWithDirection(typesArg2H,typesArg2B,owlModel)!=0||anyIPsWithDirection(typesArg2B,typesArg2H,owlModel)!=0)){//l'ordre des args est important
        	    	    						reason=reason+"ip/ip:anyIPs"+"\n";
        	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 24);
        	    	    						edgeProp[i][j].add(3);
        	    	    					}
        	    	    				}
        	    	    			}
                	    	    }else
                	    	    	if(typeBName.equals("swrl:DifferentIndividualsAtom"))
                    	    	    {
            	    	    			//predB = ((SWRLIndividualPropertyAtom) atomB).getPropertyPredicate();
            	    	    			arg1B = (RDFResource) ((SWRLDifferentIndividualsAtom) atomB).getArgument1();
                    	    			arg2B = (RDFResource) ((SWRLDifferentIndividualsAtom) atomB).getArgument2();
                    	    			typesArg1B = getVarType(label[j],arg1B, body[j], head[j], "body", owlModel);
        	    	    				typesArg2B = getVarType(label[j],arg2B, body[j], head[j], "body", owlModel);
        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg2B);
            	    	    			
            	    	    					if(dependsOn(typesArg1H, typesArg1B) && dependsOn(typesArg2H, typesArg2B)){
            	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 20);
        	    		    				edgeProp[i][j].add(3);	
        	    		    				reason=reason+"ip/diffrentfrom"+"\n";
            	    	    			}
                    	    	    }
        	    	    		else
                	    	    	if(typeBName.equals("swrl:SameIndividualAtom"))
                    	    	    {
            	    	    			//predB = ((SWRLIndividualPropertyAtom) atomB).getPropertyPredicate();
            	    	    			arg1B = (RDFResource) ((SWRLSameIndividualAtom) atomB).getArgument1();
                    	    			arg2B = (RDFResource) ((SWRLSameIndividualAtom) atomB).getArgument2();
                    	    			typesArg1B = getVarType(label[j],arg1B, body[j], head[j], "body", owlModel);
        	    	    				typesArg2B = getVarType(label[j],arg2B, body[j], head[j], "body", owlModel);
        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg2B);
            	    	    			
            	    	    					if(dependsOn(typesArg1H, typesArg1B) && dependsOn(typesArg2H, typesArg2B)){
            	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 21);
        	    		    				edgeProp[i][j].add(3);	
        	    		    				reason=reason+"ip/sameas"+"\n";
            	    	    			}
                    	    	    }
        	    	    		else
                	    	    	if(typeBName.equals("swrl:ClassAtom")){
                	    	    		predB = (((SWRLClassAtom) atomB).getClassPredicate());
                	    	    		typesArg1B = getVarType(label[j],(SWRLVariable)((SWRLClassAtom) atomB).getArgument1() , body[j], head[j], "body", owlModel);
                	    	    		
                	    	    		insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
                	    	    		trouve = false;
                	    	    		for(int t=0; t<typesArg1H.size()&& !trouve;t++){
                	    	    			//reason=reason+predH.getBrowserText()+" : "+((RDFSClass)typesArg1H.elementAt(t)).getBrowserText()+"/"+((RDFSClass)predB).getBrowserText()+"\n";
                	    				if(equalOrEquivalentOrSubOf((RDFSClass)typesArg1H.elementAt(t), (RDFSClass)predB)){
                	    					myMatrix[i][j]=Math.max(myMatrix[i][j], 3);
        	    		    				edgeProp[i][j].add(2);
        	    		    				reason=reason+"ip/cls"+"\n";
        	    		    				trouve=true;
        	    		    			}	
                	    	    	}
                	    	    	}            	    	    
        	    	    		}
        	    		
        	    		if(
            	    	    	typeHName.equals("swrl:SameIndividualAtom")
            	    	    	)/*TODO SameAs*/
                	    	    {
            	    	    		//predH = SWRLSameIndividualAtom;
                	    			arg1H = (RDFResource) ((SWRLSameIndividualAtom) atomH).getArgument1();
                	    			arg2H = (RDFResource) ((SWRLSameIndividualAtom) atomH).getArgument2();
                	    			//System.out.println(arg2H.getRDFType().getBrowserText());
                	    			typesArg1H = getVarType(label[i],/*(SWRLVariable)*/arg1H, body[i], head[i], "head", owlModel);
    	    	    				typesArg2H = getVarType(label[i],/*(SWRLVariable)*/arg2H, body[i], head[i], "head", owlModel);
    	    	    				insertWithoutRedendency(headCls.elementAt(i), typesArg1H);
    	    	    				insertWithoutRedendency(headCls.elementAt(i), typesArg2H);
    	    	    				//IP/IP
            	    	    		if(typeBName.equals("swrl:SameIndividualAtom"))
                    	    	    {
            	    	    			//predB = ((SWRLIndividualPropertyAtom) atomB).getPropertyPredicate();
            	    	    			arg1B = (RDFResource) ((SWRLSameIndividualAtom) atomB).getArgument1();
                    	    			arg2B = (RDFResource) ((SWRLSameIndividualAtom) atomB).getArgument2();
                    	    			typesArg1B = getVarType(label[j],/*(SWRLVariable)*/arg1B, body[j], head[j], "body", owlModel);
        	    	    				typesArg2B = getVarType(label[j],/*(SWRLVariable)*/arg2B, body[j], head[j], "body", owlModel);
        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg2B);
            	    	    			
            	    	    					if(dependsOn(typesArg1H, typesArg1B) && dependsOn(typesArg2H, typesArg2B)){
            	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 37);
        	    		    				edgeProp[i][j].add(3);	
        	    		    				reason=reason+"sameas/sameas"+"\n";
            	    	    			}else//(alg 3)ajout de la possibilité de l'existence d'une IP entre les deux types des args ayant la mm position
            	    	    			{
            	    	    			if((anyIPsWithDirection(typesArg1H,typesArg1B,owlModel)!=0 || anyIPsWithDirection(typesArg1B,typesArg1H,owlModel)!=0) 
    									&& (anyIPsWithDirection(typesArg2H,typesArg2B,owlModel)!=0 || anyIPsWithDirection(typesArg2B,typesArg2H,owlModel)!=0)){//l'ordre des args est important
            	    	    				reason=reason+"sameas/sameas:anyIPs"+"\n";
            	    	    				myMatrix[i][j]=Math.max(myMatrix[i][j], 23);
    	    		    					edgeProp[i][j].add(3);
            	    	    				}
            	    	    		
            	    	    			}
                    	    	    }else
                    	    	    	if(typeBName.equals("swrl:ClassAtom")){
                    	    	    		predB = (((SWRLClassAtom) atomB).getClassPredicate());
                    	    	    		typesArg1B = getVarType(label[j],(SWRLVariable)((SWRLClassAtom) atomB).getArgument1() , body[j], head[j], "body", owlModel);
                    	    	    		
                    	    	    		insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
                    	    	    		trouve = false;
                    	    	    		for(int t=0; t<typesArg1H.size()&& !trouve;t++){
                    	    	    			//reason=reason+predH.getBrowserText()+" : "+((RDFSClass)typesArg1H.elementAt(t)).getBrowserText()+"/"+((RDFSClass)predB).getBrowserText()+"\n";
                    	    				if(equalOrEquivalentOrSubOf((RDFSClass)typesArg1H.elementAt(t), (RDFSClass)predB)){
                    	    					myMatrix[i][j]=Math.max(myMatrix[i][j], 3);
            	    		    				edgeProp[i][j].add(2);
            	    		    				reason=reason+"sameas/cls"+"\n";
            	    		    				trouve=true;
            	    		    			}	
                    	    	    	}
                    	    	    	}            	    	    
            	    	    		}
        	    		
        	    		
        	    		if(
            	    	    	typeHName.equals("swrl:DifferentIndividualsAtom")
            	    	    	)/*TODO DiffrentFrom*/
                	    	    {
            	    	    		
                	    			arg1H = (RDFResource) ((SWRLDifferentIndividualsAtom) atomH).getArgument1();
                	    			arg2H = (RDFResource) ((SWRLDifferentIndividualsAtom) atomH).getArgument2();
                	    			//System.out.println(arg2H.getRDFType().getBrowserText());
                	    			typesArg1H = getVarType(label[i],arg1H, body[i], head[i], "head", owlModel);
    	    	    				typesArg2H = getVarType(label[i],arg2H, body[i], head[i], "head", owlModel);
    	    	    				insertWithoutRedendency(headCls.elementAt(i), typesArg1H);
    	    	    				insertWithoutRedendency(headCls.elementAt(i), typesArg2H);
            	    	    		if(typeBName.equals("swrl:DifferentIndividualsAtom"))
                    	    	    {
            	    	    			//predB = ((SWRLIndividualPropertyAtom) atomB).getPropertyPredicate();
            	    	    			arg1B = (RDFResource) ((SWRLDifferentIndividualsAtom) atomB).getArgument1();
                    	    			arg2B = (RDFResource) ((SWRLDifferentIndividualsAtom) atomB).getArgument2();
                    	    			typesArg1B = getVarType(label[j],arg1B, body[j], head[j], "body", owlModel);
        	    	    				typesArg2B = getVarType(label[j],arg2B, body[j], head[j], "body", owlModel);
        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg2B);
            	    	    			
            	    	    					if(dependsOn(typesArg1H, typesArg1B)||dependsOn(typesArg2H, typesArg2B)){
            	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 36);
        	    		    				edgeProp[i][j].add(3);	
        	    		    				reason=reason+"diffrentfrom/diffrentfrom"+"\n";
            	    	    			}else//(alg 3)ajout de la possibilité de l'existence d'une IP entre les deux types des args ayant la mm position
            	    	    			{
            	    	    			if((anyIPsWithDirection(typesArg1H,typesArg1B,owlModel)!=0 ||anyIPsWithDirection(typesArg1B,typesArg1H,owlModel)!=0) 
    									&& (anyIPsWithDirection(typesArg2H,typesArg2B,owlModel)!=0 ||anyIPsWithDirection(typesArg2B,typesArg2H,owlModel)!=0)){//l'ordre des args est important
            	    	    				reason=reason+"diffrentfrom/diffrentfrom:anyIPs"+"\n";
            	    	    				myMatrix[i][j]=Math.max(myMatrix[i][j], 22);
    	    		    					edgeProp[i][j].add(3);
            	    	    				}
            	    	    		
            	    	    			}
                    	    	    }else
                    	    	    	if(typeBName.equals("swrl:ClassAtom")){
                    	    	    		predB = (((SWRLClassAtom) atomB).getClassPredicate());
                    	    	    		typesArg1B = getVarType(label[j],(SWRLVariable)((SWRLClassAtom) atomB).getArgument1() , body[j], head[j], "body", owlModel);
                    	    	    		
                    	    	    		insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
                    	    	    		trouve = false;
                    	    	    		for(int t=0; t<typesArg1H.size()&& !trouve;t++){
                    	    	    			//reason=reason+predH.getBrowserText()+" : "+((RDFSClass)typesArg1H.elementAt(t)).getBrowserText()+"/"+((RDFSClass)predB).getBrowserText()+"\n";
                    	    				if(equalOrEquivalentOrSubOf((RDFSClass)typesArg1H.elementAt(t), (RDFSClass)predB)){
                    	    					myMatrix[i][j]=Math.max(myMatrix[i][j], 3);
            	    		    				edgeProp[i][j].add(2);
            	    		    				reason=reason+"diffrentfrom/cls"+"\n";
            	    		    				trouve=true;
            	    		    			}	
                    	    	    	}
                    	    	    	}            	    	    
            	    	    		}
        	    		
        	    		
        	    	    	else
                	    	    	if(typeHName.equals("swrl:DatavaluedPropertyAtom"))/*TODO dp*/
                    	    	    {
                	    	    		predH = (((SWRLDatavaluedPropertyAtom) atomH).getPropertyPredicate());
                    	    			arg1H = (RDFResource) ((SWRLDatavaluedPropertyAtom) atomH).getArgument1();
                    	    			//arg2H = (RDFResource) ((SWRLIndividualPropertyAtom) atomH).getArgument2();
                    	    			typesArg1H = getVarType(label[i],/*(SWRLVariable)*/arg1H, body[i], head[i], "head", owlModel);
                    	    			insertWithoutRedendency(headCls.elementAt(i), typesArg1H);
                	    	    		if(typeBName.equals("swrl:DatavaluedPropertyAtom"))//DP/DP
                        	    	    {
                	    	    			predB = ((SWRLDatavaluedPropertyAtom) atomB).getPropertyPredicate();
                	    	    			arg1B = (RDFResource) ((SWRLDatavaluedPropertyAtom) atomB).getArgument1();
                	    	    			typesArg1B = getVarType(label[j],/*(SWRLVariable)*/arg1B, body[j], head[j], "body", owlModel);
                	    	    			insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
                	    	    			if(equalOrEquivalentOrSubOf((DefaultOWLDatatypeProperty)predH,(DefaultOWLDatatypeProperty)predB)
                	    	    				&& (dependsOn(typesArg1H, typesArg1B))){
                	    	    				myMatrix[i][j]=Math.max(myMatrix[i][j], 35);
            	    		    				edgeProp[i][j].add(3);
            	    		    				reason=reason+"dp/dp"+"\n";
                	    	    			}
                        	    	    }else
                        	    	    	if(typeBName.equals("swrl:ClassAtom")){//DP/CLS
                        	    	    		predB = (((SWRLClassAtom) atomB).getClassPredicate());
                        	    	    		typesArg1B = getVarType(label[j],(SWRLVariable)((SWRLClassAtom) atomB).getArgument1() , body[j], head[j], "body", owlModel);
                        	    	    		insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
                        	    	    		trouve = false;
                        	    	    		for(int t=0; t<typesArg1H.size()&& !trouve;t++){
                        	    				if(equalOrEquivalentOrSubOf((RDFSClass)typesArg1H.elementAt(t), (RDFSClass)predB)){
                        	    					myMatrix[i][j]=Math.max(myMatrix[i][j], 3);
                	    		    				edgeProp[i][j].add(2);
                	    		    				reason=reason+"dp/cls"+"\n";
                	    		    				trouve=true;
                	    		    			}	
                        	    	    	}
                        	    	    	}
                    	    	    }else
                    	    	    	if(typeHName.equals("swrl:BuiltinAtom"))
                    	    	    	{ 
                    	    	    	  
                    	    	    		List args =  ((SWRLBuiltinAtom) atomH).getArguments().getValues();
                    	    	    		RDFResource r;
                    	    	    		Vector<RDFSClass> Carg = new Vector();//Les types possibles de l'argument en cours
                    	    	    		for(Iterator itl = args.iterator();itl.hasNext();){
                      							r = (RDFResource) itl.next();
                      							Carg = getVarType(label[i],(RDFResource)r, body[i], head[i], "head",owlModel);
                      							/*if(r.getClass().getSimpleName().equals("DefaultRDFSNamedClass")){
                      								Carg.addElement((RDFSClass) r);
                      							}else
                      								if(r.getClass().getSimpleName().equals("DefaultSWRLVariable")){
                      									Carg = getVarType(label[i],(SWRLVariable)r, body[i], head[i], "head",owlModel);
                      								}*/
                      							insertWithoutRedendency(headCls.elementAt(i), Carg);
                    	    	    		if(typeBName.equals("swrl:ClassAtom")){
                        	    	    		predB = (((SWRLClassAtom) atomB).getClassPredicate());
                        	    	    		trouve = false;
                        	    	    		Vector<RDFSClass> v = new Vector<RDFSClass>();
                        	    	    		v.addElement((RDFSClass)predB);
                        	    	    		//insertWithoutRedendency(bodyCls.elementAt(j), v);
                        	    	    		if(anyIPsWithDirection(Carg, v,owlModel)!=0){
                        	    	    			//inFile("D:/c.txt", "\nA_IP: "+label[i]+" --> "+label[j]+"\n",true);
                        	    	    			reason=reason+"bi/cls:anyIPsWithDirection"+"\n";
                        	    	    			myMatrix[i][j]=Math.max(myMatrix[i][j], 1);
                	    		    				edgeProp[i][j].add(2);
                	    		    				trouve=true;
                        	    	    		}
                        	    	    		if(//anyIPsWithDirection(Carg, v,owlModel)||
                        	    	    		 //anyIPsWithDirection(Carg,v ,owlModel)||
                        	    	    				dependsOn(v,Carg)||
                        	    	    				dependsOn(Carg,v)){
                        	    	    			//recherche des IPs
                        	    	    			myMatrix[i][j]=Math.max(myMatrix[i][j], 1);
                	    		    				edgeProp[i][j].add(2);
                	    		    				reason=reason+"bi/cls"+"\n";
                	    		    				trouve=true;
                        	    	    		}
                        	    	    			
                        	    	    		
                        	    	    	}else
                        	    	    		if(typeBName.equals("swrl:IndividualPropertyAtom"))
                                	    	    {
                        	    	    			
                        	    	    			predB = ((SWRLIndividualPropertyAtom) atomB).getPropertyPredicate();
                        	    	    			arg1B = (RDFResource) ((SWRLIndividualPropertyAtom) atomB).getArgument1();
                                	    			arg2B = (RDFResource) ((SWRLIndividualPropertyAtom) atomB).getArgument2();
                                	    			typesArg1B = getVarType(label[j],/*(SWRLVariable)*/arg1B, body[j], head[j], "body", owlModel);
                    	    	    				typesArg2B = getVarType(label[j],/*(SWRLVariable)*/arg2B, body[j], head[j], "body", owlModel);
                    	    	    				if(dependsOn(Carg, typesArg1B)){
                    	    	    					reason=reason+"bi/ip"+"\n";
                    	    	    					myMatrix[i][j]=Math.max(myMatrix[i][j], 1);
                        	    		    				edgeProp[i][j].add(3);	
                            	    	    			}
                        	    	    			
                        	    	    				if(anyIPsWithDirection(Carg, typesArg1B,owlModel)!=0/*||anyIPsWithDirection(Carg, typesArg2B,owlModel)*/){
                        	    	    				//inFile("D:/c.txt", "\nA_IP: "+label[i]+" --> "+label[j]+"\n",true);
                        	    	    					reason=reason+"bi/ip:anyIPsWithDirection"+"\n";
                        	    	    					myMatrix[i][j]=Math.max(myMatrix[i][j], 1);
                    	    		    				edgeProp[i][j].add(3);	
                        	    	    			}
                                	    	    }else
                                	    	    	if(typeBName.equals("swrl:DifferentIndividualsAtom"))
                                    	    	    {
                            	    	    			//predB = ((SWRLIndividualPropertyAtom) atomB).getPropertyPredicate();
                            	    	    			arg1B = (RDFResource) ((SWRLDifferentIndividualsAtom) atomB).getArgument1();
                                    	    			arg2B = (RDFResource) ((SWRLDifferentIndividualsAtom) atomB).getArgument2();
                                    	    			typesArg1B = getVarType(label[j],/*(SWRLVariable)*/arg1B, body[j], head[j], "body", owlModel);
                        	    	    				typesArg2B = getVarType(label[j],/*(SWRLVariable)*/arg2B, body[j], head[j], "body", owlModel);
                        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
                        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg2B);
                            	    	    			
                            	    	    					if(dependsOn(Carg, typesArg1B)||dependsOn(Carg, typesArg2B)){
                            	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 1);
                        	    		    				edgeProp[i][j].add(3);	
                        	    		    				reason=reason+"bi/diffrentfrom"+"\n";
                            	    	    			}
                                    	    	    }
                        	    	    		else
                                	    	    	if(typeBName.equals("swrl:SameIndividualAtom"))
                                    	    	    {
                            	    	    			//predB = ((SWRLIndividualPropertyAtom) atomB).getPropertyPredicate();
                            	    	    			arg1B = (RDFResource) ((SWRLSameIndividualAtom) atomB).getArgument1();
                                    	    			arg2B = (RDFResource) ((SWRLSameIndividualAtom) atomB).getArgument2();
                                    	    			typesArg1B = getVarType(label[j],/*(SWRLVariable)*/arg1B, body[j], head[j], "body", owlModel);
                        	    	    				typesArg2B = getVarType(label[j],/*(SWRLVariable)*/arg2B, body[j], head[j], "body", owlModel);
                        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg1B);
                        	    	    				insertWithoutRedendency(bodyCls.elementAt(j), typesArg2B);
                            	    	    			
                            	    	    					if(dependsOn(Carg, typesArg1B)||dependsOn(Carg, typesArg2B)){
                            	    	    						myMatrix[i][j]=Math.max(myMatrix[i][j], 1);
                        	    		    				edgeProp[i][j].add(3);	
                        	    		    				reason=reason+"bi/sameas"+"\n";
                            	    	    			}
                                    	    	    }
                        	    	    		else
                                	    	    	if(typeBName.equals("swrl:DatavaluedPropertyAtom"))
                                    	    	    {
                            	    	    			
                            	    	    			predB = ((SWRLDatavaluedPropertyAtom) atomB).getPropertyPredicate();
                            	    	    			arg1B = (RDFResource) ((SWRLDatavaluedPropertyAtom) atomB).getArgument1();
                                    	    			//arg2B = (RDFResource) ((SWRLDatavaluedPropertyAtom) atomB).getArgument2();
                                    	    			typesArg1B = getVarType(label[j],/*(SWRLVariable)*/arg1B, body[j], head[j], "body", owlModel);
                        	    	    				//typesArg2B = getVarType(label[j],/*(SWRLVariable)*/arg2B, body[j], head[j], "body", owlModel);
                        	    	    				if(dependsOn(Carg, typesArg1B)){
                        	    	    					reason=reason+"bi/dp"+"\n";
                        	    	    					myMatrix[i][j]=Math.max(myMatrix[i][j], 1);
                            	    		    				edgeProp[i][j].add(3);	
                                	    	    			}
                                    	    	    }
                    	    	    		}//parcours des args du BI
                    	    	    	}//BI
        	    		
        		    }//for itB
    		    }//for itH
     	
    			}//i!=j
    			inFile("D:/new.txt", reason, true);
    			/* Analyse du résultat  */
	     		
    		
	     		/* if(label[i].equals("Rule-83") && label[j].equals("Rule-9"))
	     			System.out.println("*******"+label[i]+"->" +label[j]+" : "+reason);			
    		
	     		 if(label[i].equals("Rule-9") && label[j].equals("Rule-83"))
	     			System.out.println("*******"+label[i]+"->" +label[j]+" : "+reason);	
	     		 */
	     		
	    					
    		}//for R1
    		
    	}//for R2
    	
    	/*TODO ELIMINATION DES CYCLES AVEC PONDERATION
    	 * for(int i=0; i<size; i++)
    		for(int j=0; j<size; j++){
    			if(myMatrix[i][j]!=0 && myMatrix[j][i]!=0){
    				if(myMatrix[i][j]>myMatrix[j][i])
    					myMatrix[j][i]=0;
    				else
    					if(myMatrix[j][i]>myMatrix[i][j])
    						myMatrix[i][j]=0;
    			}
    			if(label[i].equals("Rule-3") && label[j].equals("Rule-81"))
    			System.out.println("*******"+myMatrix[i][j]+"-" +myMatrix[j][i]);		
    		}*/
    	
    	/*Résultat de l'analyse de Alg3*/
    	int c3=0, fp3=0;
    	for(int i=0; i<size; i++)
    		for(int j=0; j<size; j++){
    			if(matrixExact[i][j]!=0 && myMatrix[i][j]!=0 ){
    				inFileln("D:/c.txt","C3: "+label[i]+"->"+label[j],true);
    			c3++;
    			}
    			else
    			
    				{if(matrixExact[i][j]== 0 && myMatrix[i][j]!=0 )
    					if(

  	    					   !(
  	    							   		   ((getRuleNum(label[i])>4&&getRuleNum(label[i])<8) &&
  	    	    								(getRuleNum(label[j])>4&&getRuleNum(label[j])<8))||
  	    	    		    				   ((getRuleNum(label[i])>-1&&getRuleNum(label[i])<4) && 
  	    	    		    				    (getRuleNum(label[j])>-1&&getRuleNum(label[j])<4))||	
  	    	    		    				   ((getRuleNum(label[i])>7&&getRuleNum(label[i])<24)&&
  	    	    		    				    (getRuleNum(label[j])>7&&getRuleNum(label[j])<24)))


 	    		    				   ){
    					inFileln("D:/fp.txt", "f3+: "+label[i]+"->"+label[j], true);	
    					fp3++;
    					}
    				}
    				
    		}
    	System.out.println("******algorithme3''-******");
    	System.out.println("dépendances correctes : " + c3 + "/"+tot);
    	System.out.println("Faux positifs : " + fp3);
    	int ri2rj=0, rj2ri=0;
	
    	
    	/*TODO alg 8hh :  
		 * dependances avec anyIPsWithDirection entre heads seulement 
    	* */
  

    	for(int i=0; i<size; i++){
    		for(int j=0; j<size; j++){
    		reason="";
    		
    		myMatrix8hh[i][j]=anyIPsWithDirection(headCls.elementAt(i), headCls.elementAt(j), owlModel);
    			if(label[i].equals("Rule-81")&&label[j].equals("Rule-22"))	{
        			System.out.println("8hh:Rule-81-->Rule-22 = "+myMatrix8hh[i][j]+" : "+reason);
        		}
        		if(label[i].equals("Rule-22")&&label[j].equals("Rule-81"))	{
        			System.out.println("8hh:Rule-22-->Rule-81= "+myMatrix8hh[i][j]+" : "+reason);
        		}
    		}
    	
    		}
    	//garder l'arc dont le poids le plus fort et supprimer les cycles binaires
    	for(int i=0; i<size; i++){
    		for(int j=0; j<size; j++){
    			if(myMatrix8hh[i][j]>myMatrix8hh[j][i]){
    				myMatrix8hh[j][i]=0;
    			}else
    			if(myMatrix8hh[j][i]>myMatrix8hh[i][j]){
    				myMatrix8hh[i][j]=0;
    			}else{//myMatrix8hh[j][i] == myMatrix8hh[i][j]
    				myMatrix8hh[i][j]=0;
    				myMatrix8hh[j][i]=0;	
    			}
    			
    	
    		}
    	}
    	
    	for(int i=0; i<size; i++){
    		for(int j=0; j<size; j++){
    		if(myMatrix8hh[i][j]!=0 && myMatrix8hh[j][i]!=0){
    			System.out.println("cycle HH: "+label[i]+"--"+label[j]);
    		}
    		}
    		}
    	
    	/*Résultat de l'analyse*/
    	int c8hh=0, fp8hh=0;
    	for(int i=0; i<size; i++)
    		for(int j=0; j<size; j++){
    			if(matrixExact[i][j]!=0 && myMatrix8hh[i][j]!=0 ){
    				inFileln("D:/c.txt","C8hh: "+label[i]+"->"+label[j],true);
    			c8hh++;
    			}
    			else
    				{if(matrixExact[i][j]== 0 && myMatrix8hh[i][j]!=0 )
    					if(

  	    					   !(
  	    							   		   ((getRuleNum(label[i])>4&&getRuleNum(label[i])<8) &&
  	    	    								(getRuleNum(label[j])>4&&getRuleNum(label[j])<8))||
  	    	    		    				   ((getRuleNum(label[i])>-1&&getRuleNum(label[i])<4) && 
  	    	    		    				    (getRuleNum(label[j])>-1&&getRuleNum(label[j])<4))||	
  	    	    		    				   ((getRuleNum(label[i])>7&&getRuleNum(label[i])<24)&&
  	    	    		    				    (getRuleNum(label[j])>7&&getRuleNum(label[j])<24)))

    				    			
    				    				   ){
    						inFileln("D:/fp.txt", "f8hh+: "+label[i]+"->"+label[j],true);
    					fp8hh++;
    					}
    				}
    			
    		}
    	System.out.println("******  algorithme 8hh ******");
    	System.out.println("dépendances correctes : " + c8hh + "/"+tot);
    	System.out.println("Faux positifs : " + fp8hh);
    	
    	
    	 /*TODO modification alg3+8hb par alg8hh*/
    	for(int i=0; i<size; i++){
    		for(int j=0; j<size; j++){
    			
    			if((myMatrix[i][j]==0 && myMatrix[j][i]==0) || (myMatrix[i][j]!=0 && myMatrix[j][i]!=0)){//pas de dépendances  ou il existe un cycle binaire entre i et j
    					
    				myMatrix[i][j]=myMatrix8hh[i][j];
    				myMatrix[j][i]=myMatrix8hh[j][i];
    			
    			}
    			else //correction du sens de la dépendance
    				if(myMatrix[i][j]<myMatrix8hh[j][i]){
    					myMatrix[i][j]=0;
        				myMatrix[j][i]=myMatrix8hh[j][i];
    				}
    			
    		}//for j
    	}//for i
    	
    	for(int i=0; i<size; i++){
    		for(int j=0; j<size; j++){
    			if(myMatrix[i][j]!=0 && myMatrix[j][i]!=0){
    				//System.out.println("cycle: "+ label[i]+"-"+label[j]+" = "+myMatrix[i][j]+"-"+myMatrix[j][i]);
    				myMatrix[i][j]=0;  myMatrix[j][i]=0;
    			}	
    		}
    		}
    	
    	
    	/*Résultat de l'analyse alg3+8hb amélioré par alg8hh*/
    	c3=0; fp3=0;
    	for(int i=0; i<size; i++)
    		for(int j=0; j<size; j++){
    			if(matrixExact[i][j]!=0 && myMatrix[i][j]!=0 ){
    				inFileln("D:/c.txt","C3+8hh: "+label[i]+"->"+label[j],true);
    			c3++;
    			}
    			else
    			
    				{if(matrixExact[i][j]== 0 && myMatrix[i][j]!=0 )
    					if(

  	    					   !(
  	    							   		   ((getRuleNum(label[i])>4&&getRuleNum(label[i])<8) &&
  	    	    								(getRuleNum(label[j])>4&&getRuleNum(label[j])<8))||
  	    	    		    				   ((getRuleNum(label[i])>-1&&getRuleNum(label[i])<4) && 
  	    	    		    				    (getRuleNum(label[j])>-1&&getRuleNum(label[j])<4))||	
  	    	    		    				   ((getRuleNum(label[i])>7&&getRuleNum(label[i])<24)&&
  	    	    		    				    (getRuleNum(label[j])>7&&getRuleNum(label[j])<24)))

 	    		    				   ){
    					inFileln("D:/fp.txt", "f3+8hh: "+label[i]+"->"+label[j], true);	
    					fp3++;
    					}
    				}
    				
    		}
    	System.out.println("******algorithme3'+8hh******");
    	System.out.println("dépendances correctes : " + c3 + "/"+tot);
    	System.out.println("Faux positifs : " + fp3);
    
	
    

    /////////* TODO Groupement *//////////	
    	HashSet[][] inOut=new HashSet[size][2];
    	for(int i=0; i<size;i++){
    		for(int j=0; j<2; j++){
    			inOut[i][j]= new HashSet();
    		}
    		}
    			
    	//in=inOut[0], out=inOut[1], inOut[k][] correspond à la règle label[k]
    	
    	for(int i=0; i<size; i++){
    		for(int j=0; j<size; j++){
    			if(myMatrix[i][j]!=0){
    				inOut[j][0].add(label[i]);
    				inOut[i][1].add(label[j]);
    			}
    		}
    		}
    	
    	
    	/*for(int i=0; i<size; i++){
    		//for(int j=0; j<2; j++){
    			inOut[i][1].removeAll(inOut[i][0]);//remove in de out
    		}
    		//}
    	*/
    	
    	HashSet[][] inOutGrps=new HashSet[size][3];//in=inOutGrps[0], out=inOutGrps[1], rules=inOutGrps[2]
    	for(int i=0; i<size;i++){
    		for(int j=0; j<3; j++){
    			inOutGrps[i][j]= new HashSet();
    		}
    		}
    	boolean affected;
    	int index=0;
    /*  inOutGrps[0][0]=inOut[0][0];//in
    	inOutGrps[0][1]=inOut[0][1];//out
    	inOutGrps[0][2].add(label[0]);//règles du groupes
*/    	for(int i=0; i<size;i++){//parcours de inOut
    		affected=false;
    		for(int j=0; j<index && !affected; j++){//parcours de inOutGrps
    			
    			if(inOut[i][0].equals(inOutGrps[j][0]) //mm arcs entants
    			&& inOut[i][1].equals(inOutGrps[j][1])&&inOutGrps[j][2].size()!=0){ //et mm arcs sortants : arcs sortants de la règle = arcs sortants du groupe
    				inOutGrps[j][2].add(label[i]);
    				affected = true;
    				
    			}
    			
    				
    		}
    	
    		if(!affected){
    			
    			inOutGrps[index][0]=inOut[i][0];
    	    	inOutGrps[index][1]=inOut[i][1];
    	    	inOutGrps[index][2].add(label[i]);
    	    index++;
    		}
    		
    		
    		}
    	
    
    	//index++;
    	//graphe des groupes
    	grpsGrph = new int[index][index];
    	for(int i=0; i<index; i++){
    		for(int j=0; j<index; j++){
    			//in de groupe i
    			if(/*(inOutGrps[j][2].size()!=0)
    			&& */(inOutGrps[i][0].containsAll(inOutGrps[j][2])) //j.rules dans i.in
    			)
    				grpsGrph[j][i]++;
    			
    			//out de groupe i
    			if(/*(inOutGrps[i][1].size()!=0) 
    			&& */(inOutGrps[j][0].containsAll(inOutGrps[i][2]))  //i.rules dans j.in 
    			)
    				grpsGrph[i][j]++;	
    	}
	}
    	
    	
    
    	
    	/*System.out.println("-----------------------groupes: "+grpsGrph.length);
    	for(int i =0; i<index; i++){
    		for(int j=0; j<index; j++){
    			if(grpsGrph[i][j]!=0)
    				System.out.println((i)+"-->"+(j)+" : "+grpsGrph[i][j]);
    		}
    	}
    	*/
    	
    	
     /* TODO 	 suppression de la transitivité */
    	path = new int[index];//index = nombre de groupes
    		taboo = new boolean[index];
    		
    	    for(int source=0; source<index; source++){
    	    	taboo = new boolean[index];
    	    	for(int target=0; target<index; target++){
    	    		modif11(source, target, source,0);
    	    	}
    	    }
    	   
      	
      	
      	System.out.println("-----------------------groupes APRES SUPPRESSION DE LA TRANSITIVITE: "+grpsGrph.length+" = "+index);
      	for(int i =0; i<index; i++){
      		for(int j=0; j<index; j++){
      			if(grpsGrph[i][j]!=0)
      				System.out.println((i)+"-->"+(j)+" : "+grpsGrph[i][j]);
      		}
      	}
    	
    	
    	
    	/* suppression des cycles */ 
    	/*path = new int[index];//index = nombre de groupes
  		taboo = new boolean[index];
  		
  	    for(int source=0; source<index; source++){
  	    	taboo = new boolean[index];
  	    	for(int target=0; target<index; target++){
  	    		modif1(source, target, source,0);
  	    	}
  	    }
  	   */
    	/*for(int i =0; i<index; i++){
      		for(int j=0; j<index; j++){
      			for(int k=0; k<index; k++){
      				if(grpsGrph[i][j]!=0 && grpsGrph[j][k]!=0 && grpsGrph[i][k]!=0)
      					grpsGrph[i][k]=0;
      			}
      		}
      		}
    	*/
    	
    /*	
  	  System.out.println("-----------------------groupes APRES SUPPRESSION DES CYCLES: "+grpsGrph.length+" = "+index);
  	for(int i =0; i<index; i++){
  		for(int j=0; j<index; j++){
  			if(grpsGrph[i][j]!=0)
  				System.out.println((i)+"-->"+(j)+" : "+grpsGrph[i][j]);
  		}
  	}
    */
  	

    	
  	
  //affichage du resultat : rules et groupes

	inFile("D:/new.txt", "",true);
	for(int i=0; i<size; i++){
		inFileln("D:/new.txt","\n"+label[i]+"IN" ,true);
		Iterator it=inOut[i][0].iterator(); //in
		while(it.hasNext()) 
		{
			inFile("D:/new.txt",getRuleNum((String)it.next())+"-" ,true);	
		}
		inFileln("D:/new.txt","\n"+label[i]+"OUT" ,true);
		it=inOut[i][1].iterator(); //out
		while(it.hasNext()) 
		{
			inFile("D:/new.txt",getRuleNum((String)it.next())+"-" ,true);	
		}
		}
	
	
	
	
	
	
	for(int i=0; i<index; i++){
		inFileln("D:/new.txt","Gr "+i,true);
		
		Iterator it;
		/*it=inOutGrps[i][0].iterator(); 
		while(it.hasNext()) 
		{
			inFile("D:/new.txt",getRuleNum((String)it.next())+"-" ,true);	
		}
		inFile("D:/new.txt",":\nOUT\n" ,true);
		it=inOutGrps[i][1].iterator(); 
		while(it.hasNext()) 
		{
			inFile("D:/new.txt",getRuleNum((String)it.next())+"-" ,true);	
		}
		inFile("D:/new.txt",":\nRULES\n" ,true);*/
		it=inOutGrps[i][2].iterator(); 
		while(it.hasNext()) 
		{
			inFile("D:/new.txt",getRuleNum((String)it.next())+"-" ,true);	
		}
		
		}
   	
/////////////////////////////

    	
    	//DirectedSparseGraph graph = new DirectedSparseGraph();
    	DirectedGraph graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class); 

    	for(int i=0; i<size; i++){
    		graph.addVertex(label[i]);
    	}
    	for(int i=0; i<size; i++)
    		for(int j=0; j<size; j++){
    			if(myMatrix8[i][j]!=0){
    				graph.addEdge(label[i], label[j]);
    			}
    		}
    	
    	
    	//HawickJamesSimpleCycles  	cd = new HawickJamesSimpleCycles((DirectedGraph) graph);
    		//List cycles= getHawickJamesSimpleCycles(myMatrix);
    		//System.out.println("cycles="+ cycles.toArray().length);
    		/*for(int i=0; i< cycles.size(); i++){
    			System.out.println(cycles.get(i).getClass());
    		}*/


    
    	
/////////////rules group////////
TestTreeTable tt = new TestTreeTable(owlModel); 
List<Rule> lr = tt.lr;
Vector<List<Rule>> groups = new Vector<List<Rule>>();//chaque ligne du vector est un groupe de règles
Vector<String> groupsNames = new Vector<String>();//chaque position i de ce vector correspont au nom du groupe à la ligne i du vector groups


String gn; List<Rule> l;
for(int i=0; i<lr.size();i++){

gn = ((Rule)lr.get(i)).getRuleGroupName();
if(!groupsNames.contains(gn)){
	groupsNames.add(gn);
	l = new TreeList<Rule>();
	l.add((Rule)lr.get(i));
	groups.add(l);
}else
{
	int j;
	for(j=0; j<groupsNames.size(); j++){
		if(groupsNames.elementAt(j).equals(gn))
			break;
	}
	l = groups.elementAt(j);
	l.add((Rule)lr.get(i));
	groups.set(j, l);
}
}

groupGraph = new int[groups.size()][groups.size()];
int ff=0;
int g1=-1; int g2=-1; int r1=-1; int r2=-1;
/*for(int i=0; i<size; i++){
for(int j=0; j<size; j++){
	if(lr.get(i).group.equals(lr.get(j).group)){
		r1=getIndexInMyMatrix(lr.get(i));
		r2=getIndexInMyMatrix(lr.get(j));
		myMatrix[r1][r2]=0;
	}
if(myMatrix[i][j]!=0){
	//supprimer les arcs entre les règles du mm groupe
	if(lr.get(i).group.equals(lr.get(j).group))
	{
		myMatrix[i][j]=0;
		//myMatrixAlg6[i][j]=0;
		//System.out.println("arc supprimé: "+label[i]+"-->"+label[j]+ "=" +myMatrix[i][j]);
	
	}
}//if
}//for j
}//for i
*/
for(int i=0; i<size; i++){
for(int j=0; j<size; j++){
	//else{calculer le nombre de dépendances entre chaque deux groupes
	for(int k=0; k<groups.size(); k++){//recherche des règles dans les groupes
		if(groups.elementAt(k).contains(lr.get(i))){
			//System.out.println("g=1"+groupsNames.elementAt(k)+"--- g="+lr.get(i).getRuleGroupName());
			g1=k;
		}
		if(groups.elementAt(k).contains(lr.get(j))){
			//System.out.println("g=2"+groupsNames.elementAt(k)+"--- g="+lr.get(j).getRuleGroupName());
			g2=k;
		}
		}//for k
	
	if(myMatrix8[i][j]!=0){
	groupGraph[g1][g2]++;//nbre d'arcs de g1 vers g2
	//System.out.println("arc conservé: "+label[i]+"-->"+label[j]);
	}
	//}//if different groups (else)
//}//if !=0

}//for j
}//for i


/* suppression de la transitivité*/ 
path = new int[groups.size()];//index = nombre de groupes
	taboo = new boolean[groups.size()];
	
  for(int source=0; source<groups.size(); source++){
  	taboo = new boolean[groups.size()];
  	for(int target=0; target<groups.size(); target++){
  		modif3(source, target, source,0);
  	}
  }




inFile("D:/G.txt", "",false);
System.out.println("---nb groups: "+groups.size());
for(int i=0; i<groups.size(); i++){
for(int j=0; j<groups.size(); j++){
	if(groupGraph[i][j]!=0){
	inFileln("D:/G.txt", (i+1)+"-->"+(j+1)+": "+groupGraph[i][j],true);
	}
		
}
}


/*Résultat de l'analyse*/
int c10=0, fp10=0;
for(int i=0; i<size; i++)
	for(int j=0; j<size; j++){
		if(matrixExact[i][j]!=0 && myMatrix[i][j]!=0 ){
			inFile("D:/c.txt","C10: "+label[i]+"->"+label[j],true);
		c10++;
		}
		else
			{if(matrixExact[i][j]== 0 && myMatrix[i][j]!=0 )
				
						if(

	 	    					   !(
	 	    							   		   ((getRuleNum(label[i])>4&&getRuleNum(label[i])<8) &&
	 	    	    								(getRuleNum(label[j])>4&&getRuleNum(label[j])<8))||
	 	    	    		    				   ((getRuleNum(label[i])>-1&&getRuleNum(label[i])<4) && 
	 	    	    		    				    (getRuleNum(label[j])>-1&&getRuleNum(label[j])<4))||	
	 	    	    		    				   ((getRuleNum(label[i])>7&&getRuleNum(label[i])<24)&&
	 	    	    		    				    (getRuleNum(label[j])>7&&getRuleNum(label[j])<24)))


 				    				   ){
					inFileln("D:/fp.txt", "f10+: "+label[i]+"->"+label[j],true);
				fp10++;
				}
			}
		
			
	}
System.out.println("******algorithme10******");
System.out.println("dépendances correctes : " + c10 + "/"+tot);
System.out.println("Faux positifs : " + fp10);


System.out.println("---------------------------------");
/////////////////////////////
for(int i=0; i<size; i++)
for(int j=0; j<size; j++){
if(myMatrix[i][j]!=0){
myMatrix[i][j]=1;
}
}

////////////////////////////// *TODO   GRAPHE PLUGIN AXIOME*/
	    for (int i = 0; i < size; ++i)
	    {
           VertexInfo currentVertex = new VertexInfo();
           currentVertex.color = 0;//random.nextInt(COLOR_COUNT);
           currentVertex.oldColor = 0;
           //currentVertex.label = "V" + i;
           currentVertex.label = label[i];
           int[] edges = new int[size];
           ArrayList[] prop = new ArrayList[size];
           for (int j = 0; j < edges.length; j++)
           {
               edges[j] = 0;
               prop[j] = new ArrayList();
               tmp = myMatrix[i][j];
               while (tmp > 0)
               {
            	   prop[j].
            	   add(
            			   edgeProp[i][j].
            			   get(edges[j]));
                   edges[j]++;
                   tmp--;
               }
           }
           currentVertex.edges = edges;
           currentVertex.prop = prop;
           vertexList.add(currentVertex);
	    }
	    
	    CycleDetection cyc = new CycleDetection();
	    ArrayList[][] myPath = cyc.floyd(myMatrix);
	    
	    int s;
	    int e;
	    for(int i=0; i<size; i++)
	    {
	    	if(!(myPath[i][i].isEmpty()))
	    	{
	    		cycle = true;
	    		s = i;
	    		//System.out.println("WARNING = " + i);
	    		((VertexInfo) vertexList.get(i)).color = 1;
	    		((VertexInfo) vertexList.get(i)).oldColor = 1;
	    		
	    		//((VertexInfo) vertexList.get(i)).prop[i].add(0,1);
	    		
	    		/*for (int j=0; j<myPath[i][i].size(); j++)
			    {
	    			e = (Integer) myPath[i][i].get(j);
		    		((VertexInfo) vertexList.get(s)).prop[e].add(0,1);
		    		s = e;
			    }*/
	    		
	    	}
	    }
	    
	    return vertexList;
	    
	}
/*	private double[][] returnMatrix(int nodes, double[][] initialMatrix, double inflationParameter) {
		
			
			
			double markov_matrix[][] = new double[nodes][nodes];
			double expand_matrix[][] = new double[nodes][nodes];
			double inflate_matrix[][] = new double[nodes][nodes];
			
			
			
			for(int i=0;i<nodes;i++)
			{
				for(int j=0;j<nodes;j++)
				{
					markov_matrix[i][j]=initialMatrix[i][j];
				}
			}
			

			//markov matrix creation
			//normalize the matrix
			//calculate the total number of connections in each column and give the probability accordingly
			for(int i=0;i<nodes;i++)
			{
				double sumColumn=0;
				for(int j=0;j<nodes;j++)
				{
					if(markov_matrix[j][i]==1)
					{
						sumColumn++;
					}
				}
				
				for(int j=0;j<nodes;j++)
				{
					if(markov_matrix[j][i]==1)
					{
						markov_matrix[j][i]=(double)1/sumColumn;
					}
				}
			}
			
			int level=0;
			
			//creation of expanded matrix
			while(true)
			{
				int flag=0;
				level++;
				//System.out.println(" level = "+level);
				RealMatrix matrix1 = new Array2DRowRealMatrix(markov_matrix);
				RealMatrix matrix2 = new Array2DRowRealMatrix(markov_matrix);
				RealMatrix mulMatrix = matrix1.multiply(matrix2);
				expand_matrix=mulMatrix.getData();
				
					
			//inflate matrix creation
					for(int i=0;i<nodes;i++)
					{
						for(int j=0;j<nodes;j++)
						{
							inflate_matrix[i][j]=expand_matrix[i][j];
						}
					}
					
					
					for(int i=0;i<nodes;i++)
					{
						double sumSquare=0;
						for(int j=0;j<nodes;j++)
						{
							sumSquare = sumSquare + Math.pow(inflate_matrix[j][i],inflationParameter);
						}
						for(int j=0;j<nodes;j++)
						{
							if(sumSquare!=0)
							inflate_matrix[j][i]= (Math.pow(inflate_matrix[j][i],inflationParameter))/sumSquare;
						}
					}
					
					//check whether both the matrices converge
					for(int i=0;i<nodes;i++)
					{
						for(int j=0;j<nodes;j++)
						{
							if(inflate_matrix[i][j]!=markov_matrix[i][j])
							{
								flag=1;
								break;
							}
						}
						if(flag==1)
						{
							break;
						}
					}
					
					if(flag==0)
					{
						break;//break from while
					}
					else
					{
						RealMatrix newMat = new Array2DRowRealMatrix(inflate_matrix);
						markov_matrix=newMat.getData();
					}
					
			}//end of while
			return markov_matrix;
		}
*/
	private void afficheCollection(List<String> c) {
		
		Iterator it = c.iterator() ;
		
		 while (it.hasNext()) {
			 
		    System.out.print(" -> "+(String)it.next()) ;
		    
		}
		 System.out.println();
	}
	
	


int getRuleIndex(String name){
	for(int i=0; i<label.length;i++)
		if(label[i].equals(name))
			return i;

return -1;
}

/*class Graph
{
	private int V; // No. of vertices
	private LinkedList<Integer> adj[]; // Adjacency List

	//Constructor
	Graph(int v, int[][] matrix)//v = size
	{
		V = v;
		adj = new LinkedList[v];
		for (int i=0; i<v; ++i)
			adj[i] = new LinkedList();
		for (int i=0; i<v; ++i)
			for (int j=0; j<v; ++j)
				if(matrix[i][j]!=0)
				addEdge(i, j);
	}

	// Function to add an edge into the graph
	void addEdge(int v,int w) { adj[v].add(w); }

	// A recursive function used by topologicalSort
	void topologicalSortUtil(int v, Boolean visited[],Stack stack)
	{
		// Mark the current node as visited.
		visited[v] = true;
		Integer i;

		// Recur for all the vertices adjacent to this vertex
		Iterator<Integer> it = adj[v].iterator();
		while (it.hasNext())
		{
			i = it.next();
			if (!visited[i])
				topologicalSortUtil(i, visited, stack);
		}

		// Push current vertex to stack which stores result
		stack.push(new Integer(v));
	}

	// The function to do Topological Sort. It uses recursive
	// topologicalSortUtil()
	Stack topologicalSort()
	{
		Stack stack = new Stack();

		// Mark all the vertices as not visited
		Boolean visited[] = new Boolean[V];
		for (int i = 0; i < V; i++)
			visited[i] = false;

		// Call the recursive helper function to store Topological
		// Sort starting from all vertices one by one
		for (int i = 0; i < V; i++)
			if (visited[i] == false)
				topologicalSortUtil(i, visited, stack);
return stack;
		// Print contents of stack
		while (stack.empty()==false)
			System.out.print(stack.pop() + " ");
	}

	// Driver method
	public static void main(String args[])
	{
		// Create a graph given in the above diagram
		Graph g = new Graph(6);
		g.addEdge(5, 2);
		g.addEdge(5, 0);
		g.addEdge(4, 0);
		g.addEdge(4, 1);
		g.addEdge(2, 3);
		g.addEdge(3, 1);

		System.out.println("Following is a Topological " +
						"sort of the given graph");
		g.topologicalSort();
	}
}




*/



public class TestTreeTable  
{
	
  public JXTreeTable treeTable;
  OWLModel owlModel;
  int myc = 1;
  JTabbedPane tabs;
 // ElicitationTab elicTree;
  //HashMap<String, String> nameExp;
  List<Rule> lr;
  List<List> groups;
  //SWRLPanel sp;
  RuleGroupTreeTableModel ruleGroupTreeTableModel;

  public TestTreeTable(OWLModel owlModel) 
  {
	  this.owlModel = owlModel;
	//  this.tabs = sp.jtp;
	  //this.nameExp = sp.para.nameExp;
	//  this.sp = sp;
	  //System.out.println("Avant initialise testtreetable");
	  initialize(owlModel);
	  //System.out.println("Avant initialise testtreetable");
	  
  }
  
  public void initialize(OWLModel owlModel)
  {  
	  this.owlModel = owlModel;
	  ruleGroupTreeTableModel = new RuleGroupTreeTableModel();
	  groups = new TreeList();
	  lr = generateRules(owlModel);
	  //System.out.println(lr.)
	  groups.add(lr);
	  ruleGroupTreeTableModel.addRules(lr);
	  treeTable = new RuleGroupTreeTable(ruleGroupTreeTableModel, /*sp,*/ this);          
	  
 } // TestTreeTable
  

public class RuleGroupTreeTable extends JXTreeTable
  {
    RuleGroupTreeTableModel model;
    
    TestTreeTable test;

    public RuleGroupTreeTable(RuleGroupTreeTableModel model, /*SWRLPanel sp,*/ TestTreeTable test)
    {
      super(model);
     // this.sp = sp;
      this.model = model;
      this.test = test;
      
    } // RuleGroupTreeTable
  


    private class RuleHighLight extends AbstractAction
    {
    	String Name = "";
    	TreePath path;
        Object component;
        ArrayList<String> Names = new ArrayList<String>();
        
    	public RuleHighLight()
    	{
    		super("Highlight Rule");
    	}
      
	      public void actionPerformed(ActionEvent e)
	      {
	       
		
	        if (component instanceof DefaultMutableTreeTableNode) {
	          DefaultMutableTreeTableNode defNode = (DefaultMutableTreeTableNode)component;
	          if (defNode.getUserObject() instanceof RuleGroup) {
	        	  for(Iterator myit= lr.iterator(); myit.hasNext();)
	        	  {
	        		  Rule tmp = (Rule)myit.next();
	        		  RuleGroup ruleGroup = (RuleGroup)defNode.getUserObject();
	        		  
	        		  if(tmp.getRuleGroupName().equals(ruleGroup.getGroupName()))
	        		  {
	        			  Names.add(tmp.getRuleName());
	        		  }
	        	  }
	          }else if (defNode.getUserObject() instanceof Rule) {
	            Rule rule = (Rule)defNode.getUserObject();
	            Names.add(rule.getRuleName());
	          } // if
	        } // if
	      } // actionPerformed
    } 
    
    private class update extends AbstractAction
    {
    	String Name = "";
    	boolean right = false;
    	TreePath path;
        Object component;
        
    	
      
	      public void actionPerformed(ActionEvent e)
	      {
	        try{
	        	path = getPathForRow(getSelectedRow());
	        	component = path.getLastPathComponent();
	        }catch(NullPointerException er){
	        	System.out.println("No component is selected");
	        }
		
	        if (component instanceof DefaultMutableTreeTableNode) {
	          DefaultMutableTreeTableNode defNode = (DefaultMutableTreeTableNode)component;
	          if (defNode.getUserObject() instanceof RuleGroup) {
	        	  right = true;
	          }else if (defNode.getUserObject() instanceof Rule) {
	            right = true;
	          } // if
	        } // if
	                
	      
	      } // actionPerformed
    } 
    
   
   
  } // RuleGroupTreeTable

  class RuleGroupTreeTableModel extends DefaultTreeTableModel 
  {
    public static final int RuleGroupColumn = 0;
   //public static final int IsEnabledColumn = 1;
    public static final int RuleNameColumn = 1;
    public static final int RuleTextColumn = 2;
    private static final int NumberOfColumns = 3;
    
    DefaultMutableTreeTableNode rootNode;

    public RuleGroupTreeTableModel() 
    { 
      rootNode = new DefaultMutableTreeTableNode(new RuleGroup()); // Not visible; dummy rule group

      setRoot(rootNode); 
    } // RuleGroupTreeTableModel

    public void addRule(Rule rule)
    {
      boolean existingGroupFound = false;

      // Find existing group or groups and add this rule
      for (int i = 0; i < rootNode.getChildCount(); i++) {
        if (getChild(rootNode, i) instanceof DefaultMutableTreeTableNode) {
          DefaultMutableTreeTableNode defNode = (DefaultMutableTreeTableNode)getChild(rootNode, i);
          if (defNode.getUserObject() instanceof RuleGroup) {
            RuleGroup ruleGroup = (RuleGroup)defNode.getUserObject();
            if (ruleGroup.getGroupName().equals(rule.getRuleGroupName())) {
              defNode.add(new DefaultMutableTreeTableNode(rule));
              existingGroupFound = true;
            } // if
          } // if
        } // if
      } // for

      if (!existingGroupFound) {
        DefaultMutableTreeTableNode groupNode = new DefaultMutableTreeTableNode(rule.getRuleGroup());
        groupNode.add(new DefaultMutableTreeTableNode(rule));
        rootNode.add(groupNode);
      } // if

    } // addRule

    public void addRules(List<Rule> rules) { for (Rule rule : rules) addRule(rule); }

    public int getColumnCount() { return NumberOfColumns; }

    public Object getValueAt(Object node, int column) 
    {
      Object result = null;

      if (node instanceof DefaultMutableTreeTableNode) {
        DefaultMutableTreeTableNode defNode = (DefaultMutableTreeTableNode)node;
        if (defNode.getUserObject() instanceof Rule) {
          Rule rule = (Rule)defNode.getUserObject();
          switch (column) {
          //case IsEnabledColumn:
            //result = rule.getIsEnabled(); break;
          case RuleNameColumn:
            result = rule.getRuleName(); break;
          case RuleTextColumn:
            result = rule.getRuleText(); break;
          } // switch
        } else if (defNode.getUserObject() instanceof RuleGroup) {
          RuleGroup ruleGroup = (RuleGroup)defNode.getUserObject();
          switch (column) {
          case RuleGroupColumn:
        	  result = "Group" + ruleGroup.getNumber(); break;
          case RuleTextColumn:
        	  result = ruleGroup.getGroupName(); break;

              
            
          //case IsEnabledColumn:
            //result = ruleGroup.getIsEnabled(); break;
          } // switch
        } // if
      } // if
      return result;
    } // getValueAt

    public String getColumnName(int column) 
    {
      String result = "";
      
      switch (column) {
      case RuleGroupColumn:
        result = "Group"; break;
      //case IsEnabledColumn:
        //result = "Enabled"; break;
      case RuleNameColumn:
        result = "Name"; break;
      case RuleTextColumn:
        result = "Expression"; break;
      } // switch

      return result;
    } // getColumnName

    public boolean isCellEditable(Object node, int column) 
    {  
      boolean result = false;
//
//      if (node instanceof DefaultMutableTreeTableNode) {
//        DefaultMutableTreeTableNode defNode = (DefaultMutableTreeTableNode)node;
//        if (defNode.getUserObject() instanceof Rule) {
//          Rule rule = (Rule) defNode.getUserObject();
//          switch (column) {
//          case IsEnabledColumn:
//          case RuleNameColumn:
//          case RuleTextColumn:
//            result = true; break;
//          } // switch
//        } else if (defNode.getUserObject() instanceof RuleGroup) {
//          RuleGroup ruleGroup = (RuleGroup)defNode.getUserObject();
//          switch (column) {
//          case IsEnabledColumn:
//            result = true; break;
//          } // switch
//        } // if
//      }
      return result; 
    } // isCellEditable

//    public Class getColumnClass(int column) 
//    {
//      if (column == IsEnabledColumn) return Boolean.class;
//      else return super.getColumnClass(column);
//    } // getColumnClass



  } // RuleGroupTreeTableModel

 
  class Rule implements Comparable<Rule> 
  {
    private RuleGroup group = null;
    private String ruleName = "";
    private SWRLImp ruleText;// = "";
    //private Boolean isEnabled = Boolean.FALSE;
    
    public Rule() {}
    
    public Rule(RuleGroup group, String ruleName, SWRLImp ruleText) 
    {
      super();
      this.group = group;
      this.ruleName = ruleName;
      this.ruleText = ruleText;
      
      //this.isEnabled = isEnabled;
    } // Rule
    
    public String getRuleName() { return ruleName; }
    public String getRuleGroupName() { return group.getGroupName(); }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public RuleGroup getRuleGroup() { return group; }
    public void setRuleGroup(RuleGroup group) { this.group = group; }
    public String getRuleText() { return ruleText.getBrowserText(); }
   // public void setRuleText(String ruleText) { this.ruleText = ruleText; }
    //public Boolean getIsEnabled() { return isEnabled; }
    //public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    public String toString() { return ruleName + " " + ruleText + " " + getRuleGroupName(); }

    public int hashCode() 
    { 
      final int PRIME = 31;
      int result = 1;
      result = PRIME * result + ((ruleName == null) ? 0 : ruleName.hashCode());
      result = PRIME * result + ((group == null) ? 0 : group.hashCode());
      result = PRIME * result + ((ruleText == null) ? 0 : ruleText.hashCode());
      //result = PRIME * result + ((isEnabled == null) ? 0 : isEnabled.hashCode());
      return result;
    }
    
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final Rule other = (Rule)obj;
      if (group == null) { if (other.group != null) return false;
      } else if (!group.equals(other.group)) return false;
      if (ruleName == null) { if (other.ruleName != null) return false;
      } else if (!ruleName.equals(other.ruleName)) return false;
      if (ruleText == null) { if (other.ruleText != null) return false;
      } else if (!ruleText.equals(other.ruleText)) return false;
      //if (isEnabled == null) { if (other.isEnabled != null) return false;
      //} else if (!isEnabled.equals(other.isEnabled)) return false;
      return true;
    }

    public int compareTo(Rule otherObject) {
      int res = 0;
      res = otherObject.getRuleName().compareTo(getRuleName());
      if (0 == res) { res = otherObject.getRuleGroup().compareTo(getRuleGroup()); }
      if (0 == res) { res = otherObject.getRuleText().compareTo(getRuleText()); }
      //if (0 == res) { res = otherObject.getIsEnabled().compareTo(getIsEnabled()); }
      return res;
    }
  } // Rule

  class RuleGroup implements Comparable<RuleGroup> 
  {
    private String groupName = "";
    private String signature = "";
    private int number;
    //private Boolean isEnabled = Boolean.FALSE;

    public RuleGroup() {}
    
    public RuleGroup(String groupName, String signature, int number) 
    {
      super();
      this.groupName = groupName;
      this.signature = signature;
      this.number = number;
      //this.isEnabled = isEnabled;
    }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.groupName = signature; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    //public Boolean getIsEnabled() { return isEnabled; }
    //public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }

    public int hashCode() 
    { 
      final int PRIME = 31;
      int result = 1;
      result = PRIME * result + ((groupName == null) ? 0 : groupName.hashCode());
      //result = PRIME * result + ((isEnabled == null) ? 0 : isEnabled.hashCode());
      return result;
    }
    
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final RuleGroup other = (RuleGroup)obj;
      if (groupName == null) { if (other.groupName != null)return false;
      } else if (!groupName.equals(other.groupName)) return false;
      //if (isEnabled == null) { if (other.isEnabled != null) return false;
      //} else if (!isEnabled.equals(other.isEnabled)) return false;
      return true;
    }

    public int compareTo(RuleGroup otherObject) {
      int res = 0;
      res = otherObject.getGroupName().compareTo(getGroupName());
      //if (0 == res) { res = otherObject.getIsEnabled().compareTo(getIsEnabled()); }
      return res;
    } // compareTo

  } // RuleGroup
  
  
  private List<Rule> generateRules(OWLModel owlModel)
  {
	  List<Rule> result = new ArrayList<Rule>();
	  RDFProperty  hrg = owlModel.getRDFProperty("http://swrl.stanford.edu/ontologies/3.3/swrla.owl#hasRuleCategory");
	  SWRLFactory factory = new SWRLFactory(owlModel);
	  Collection rules = factory.getImps();
	  SWRLImp element;
	 
	  /* 
	  for (Iterator it=rules.iterator(); it.hasNext(); )
	  {
		  element = (SWRLImp) it.next();
		  result.add(new Rule( new RuleGroup(element.getPropertyValue(hrg)+""," ",myc), element.getLocalName(),element));
		  myc++;
	  }
	 List<Rule> result = new ArrayList<Rule>();
	  RDFProperty  hrg = owlModel.getRDFProperty("http://swrl.stanford.edu/ontologies/3.3/swrla.owl#hasRuleCategory");
	 // SWRLFactory factory = new SWRLFactory(owlModel);
	  //Collection rules = factory.getImps();
	  //SWRLImp element;
	 */
	  
	  for (int i=0; i< size; i++ )
	  {
		  
		  result.add(new Rule( new RuleGroup(rulesTab[i].getPropertyValue(hrg)+""," ",myc), rulesTab[i].getLocalName(),rulesTab[i]));
		  myc++;
	  }
	  
	  
	  Rule a;
	  ArrayList g = new ArrayList();// liste (ArrayList) des groupes construite à partir des groupes extraits par Axiomé (liste result)
	  int i=0;
	  
	  for (Iterator it=result.iterator(); it.hasNext(); )
	  {
		 a = (Rule) it.next();
		 if (g.contains(a.group.groupName)){
			 a.group.setNumber(i);
		 }else{
			 i++;
			 a.group.setNumber(i);
			 g.add(a.group.groupName);
			 
		 }
	  }

	  return result;
  }
}
}
  	










