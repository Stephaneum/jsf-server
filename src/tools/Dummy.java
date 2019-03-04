package tools;

import java.util.Random;

import mysql.Datenbank;
import objects.Beitrag;
import objects.Datei;
import objects.Nutzer;
import sitzung.Sitzung;

public class Dummy {
	
	//Vornamen und Nachnamen werden nachher gemischt! 
	static private String[][] namen = {
			{"Thomas","Müller"},
			{"Tobias","Schmidt"},
			{"Max","Schneider"},
			{"Laura","Fischer"},
			{"Emma","Weber"},
			{"Lukas","Meyer"},
			{"Sophia","Becker"},
			{"Leonie","Schulz"},
			{"Florian","Hoffmann"},
			{"Niklas","Koch"},
			{"Jenny","Richter"},
			{"Julia","Wolf"},
			{"Paul","Schröder"},
			{"Tim","Klein"},
			{"Sven","Hofmann"},
			{"Jan","Hartmann"},
			{"Ben","Weiß"},
			{"Nils","Otto"},
			{"Alexander","Heinrich"},
			{"Johannes","Brandt"},
			{"Kai","Engel"},
			{"Sebastian","Pohl"},
			{"Martin","Krause"},
			{"Christoph","Werner"},
			{"Maximilian","Walter"},
			{"Anna","Frank"},
			{"Marie","Beck"},
			{"Klara","Horn"}
	};
	
	//basierend auf den oberen namen-String
	//0 männlich, 1 weiblich
	static private int[] geschlecht = {0,0,0,1,1,0,1,1,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1};
	
	static private String[] email_domain = {
			"@yahoo.de",
			"@yahoo.com",
			"@gmail.com",
			"@gmx.de",
			"@outlook.de"
	};
	
	//Projektnamen
	static private String[] projekt_name = {
			"Ernährung der Käfer",
			"Topographie Europa",
			"Weimarer Republik",
			"Anglizismen",
			"lineare Funktionen",
			"schräger Wurf (Physik)",
			"indirect speech",
			"Parabeln (Mathe)",
			"Was sind Einzeller?",
			"Urbanisierung in England",
			"Aufbau von Atomen",
			"Wahlen in der Demokratie",
			"Deutsche Burschenschaften",
			"Kosmische Geschwindigkeiten",
			"Unser Sonnensystem",
			"Aus was besteht Luft?",
			"Aufbau der Erdatmosphäre",
			"Besiedelung des Mars-Planeten",
			"Epoche Sturm und Drang (Deutsch)"
	};
	
	//Beiträge
	static private Beitrag[] beitrag = {
			new Beitrag("Bundesfinale Jugend trainiert für Olympia  das Stephaneum ist wieder dabei.","<div style=\"text-align: justify;\">Am Sonntag startet nun bereits zum 4. Mal in Folge eine Mannschaft des Stephaneums im Bundesfinale Jugend trainiert für Olympia in Berlin."
					+ "Dafür gehen an den Start: Janin Hiesener, Weda Weber, Mandy Hüttner, Valerie Schröder, Lara Klanert, Alexandra Block, Sophia Pohl und Johanna Lohmüller."
					+ "Der Wettkampf dauert vom 18. bis 22. September und wir hoffen auf viel Daumendrücken in Aschersleben. Um alle wieder auf dem Laufenden zu halten, werden wir wieder versuchen, über Fast-Live-Ticker von den spannendsten Erlebnissen zu berichten.</div>"),
			new Beitrag("80 Schüler gehen auf Reisen", "<div style=\"text-align: justify;\">Im vergangenen Jahr haben die Stephaneer wieder an zahlreichen Wettbewerben teilgenommen, um sich im fairen Wettstreit mit anderen Schülern zu messen. Dies ist ein wichtiger Gradmesser am Stephaneum, um den eigenen Standort festzustellen und somit fürs Leben gerüstet zu sein. "
					+ "So wurde im vergangenen Schuljahr das Bundesfinale im Schwimmen erreicht, vordere Platzierungen im Landesfinale erkämpften die Basketballerinnen, die Handballer und die 3 Teams im Mädchenfußball. "
					+ "Aber auch im Europäischen Wettbewerb oder im Wettbewerb der politischen Bildung wurde erfolgreich teilgenommen. Ob beim Freistil-Wettbewerb oder bei der Mathe- bzw. Physik Landesolympiade, überall gelang es Platzierungen im Vorderfeld zu erreichen."
					+ "Deshalb entschied die Schulleitung für alle Preisträger und erfolgreichen Wettbewerbsteilnehmern eine Auszeichnungsfahrt durchzuführen. Diese findet nun am 06.09.2016 statt!"
					+ "Mittlerweile sind es 2 Fahrten, weil das Kontingent an Ausgezeichneten mittlerweile 90 Schüler umfasst. Das Ziel der Auszeichnungsfahrt durften die Schüler selbst treffen. 5 Vorschläge standen zur Wahl."
					+ "Mit großer Mehrheit entschieden sich die Schüler für folgende Ausflugsziele:"
					+ "Filmpark Babelsberg"
					+ "Serengeti-Park Hodenhagen"
					+ "Dank der Unterstützung folgender Sponsoren, alle Mitglied im Förderkreis des Stephaneums, trägt sich die Auszeichnungsfinanzierung ohne Mittel aus dem Schulhaushalt zu beanspruchen. Selbst der Eigenanteil der Schüler entfällt!"
					+ "Ein Dank von dieser Stelle an folgende Sponsoren"
					+ "Sasse Baukonzepte GmbH & Co. KG"
					+ "Schubert Touristik GmbH"
					+ "Ingenieurplanungsgesell. mbH Wohlrab, Landeck & Cie"
					+ "Autohaus Schmidt & Söhne"
					+ "Anhaltinische Fruchthof GmbH</div>"),
			new Beitrag("Schulprofile am Stephaneum",
					"<div style=\"text-align: justify;\">Europaschule, anerkannte UNESCO-Projektschule und Schule ohne Rassismus/Schule mit Courage - das Stephaneum ist gegenwärtig die einzige Schule bundesweit, die diese Kombination aus Schulprofilen aufzuweisen hat. "
					+ "Unter einem Schulprofil wird die besondere inhaltliche Ausrichtung einer Schule verstanden, die sich idealerweise im Schulprogramm, dem Unterricht in Projekten, Workshops und im gesamten übrigen schulischen Alltag wiederfindet. Schulen mit einem Profil unterscheiden sich dadurch von \"normalen\" Schulen, dass sie sich freiwillig dazu entschlossen haben, ihren Schülern mehr zu bieten als nur Unterricht im herkömmlichen Sinne.  </div>"),
			new Beitrag("Herausforderungen","<div style=\"text-align: justify;\">Die Verleihung und der Erhalt der mit den Profilen verbundenen Titel sind natürlich an die ständige Erfüllung hohe Anforderungen gebunden. Lässt der Elan bei der Umsetzung der Inhalte irgendwann nach, so kann dies sehr schnell zum Verlust der Titel führen. Der Erhalt der Titel ist an verschiedene Kriterien gebunden. Dazu gehört u.a. für jedes Profil ein umfangreicher jährlicher Bericht, in dem alle profilspezifischen Aktivitäten zu dokumentieren sind sowie die regelmäßige Teilnahme und Mitarbeit in den Netzwerken der UPS, der EU-Schulen und der SOR. Es gehört zum Pflichtprogramm, Partnerschaften zu andern Ländern zu pflegen, Workshops und Projekte durchzuführen, die Profilinhalte in den Unterricht zu implementieren und offensiv für die Werte der Schulprofile einzutreten. "
					+ "Die Durchführung von Projekten ist natürlich an einen erheblichen organisatorischen und finanziellen Aufwand gebunden, der nur bewältigt werden kann, wenn sich Lehrer und Schüler dafür aktiv und über die unterrichtliche Verpflichtung hinaus dafür einbringen. Auch die unbürokratische Unterstützung der Projekte durch Förderkreis und Ehemaligenverband sind hierfür unentbehrlich. Nicht zuletzt wegen dieser guten Voraussetzungen konnte sich das Stephaneum innerhalb der Netzwerke in Sachsen-Anhalt und auch darüber hinaus einen sehr guten Ruf erarbeiten. Nachfolgend eine Kurzvorstellung der Spezifika der einzelnen Profile und ihrer aktuellen Aktivitäten</div>"),
			new Beitrag("Ganztagsschule Gymnasium Stephaneum",
					"<div style=\"text-align: justify;\">Ab dem Schuljahr 2005/06 ist die Europaschule Stephaneum anerkannte Ganztagsschule. Auf der Grundlage des Beschlusses der Gesamtkonferenz vom 14.10.2004 wird am Stephaneum das Modell der offenen Ganztagsschule umgesetzt."
					+ "Was ist eine offene Ganztagsschule?"
					+ "Die offene Ganztagsschule orientiert sich überwiegend an der klassischen Unterrichtsstruktur der Halbtagsschule und bietet nach dem Mittagessen/Unterrichtsschluss ein zusätzliches, freiwilliges Nachmittagsprogramm."
					+ "Folgende Schwerpunkte bietet das Ganztagsprogramm am Stephaneum:"
					+ "Hausaufgabenbetreuung Montag - Donnerstag 14.00-16.00 Uhr."
					+ "Begabtenförderung für alle Klassenstufen, vor allem in Fächern Mathematik, Deutsch, Englisch, Französisch und Chemie."
					+ "Öffnung der Schule für Vereine (z. B. Lok, VfB, Spielmannzug, Stadtpfeifer)."
					+ "Zusammenarbeit mit der Kreismusikschule und der Malschule."
					+ "Kreative Freizeitgestaltung wie z. B. Chor, Theater, Webteam, Kunst, praktische Geschichte u.a."
					+ "Sport von der Förderung schwacher Schüler bis hin zur Vorbereitung auf Meisterschaften. Dabei sind folgende Sportarten im Angebot: Volleyball, Unihockey, Fußball, Tanzen, Schwimmen und Leichtathletik"
					+ "Ziel der Ganztagsschule ist u.a. auch die Veränderung der Lernkultur (z. B. Verknüpfung von Freizeitinteressen mit schulischen Aufgaben), soziales Lernen (z. B. Gemeinschaften unterschiedlicher Alterskulturen entstehen), die Erhöhung der Eigenverantwortung von Schülern (Aufsichtshilfe, Übernahme von AGs, Schülerservice-Center, Webteam) und Kooperation mit Einrichtungen der Jugendpflege (z. B. Jugendclub Wassertormühle).</div> "),
			new Beitrag("Der Verband stellt sich vor",
					"<div style=\"text-align: justify;\">Der VeSt. wurde im Jahre 1900 gegründet und gehört damit zu den ältesten Vereinigungen ehemaliger Schüler in Deutschland mit seinen rund 330 Mitgliedern unbedingt zu den stärksten. Seine erste Blütezeit hatte er in den 20er und 30er Jahren. Höhepunkte waren jeweils die jährlichen Ostertagungen in Aschersleben, zu denen die Mitglieder aus allen Teilen unseres Landes herbeiströmten. Krieg und Zusammenbruch setzten dem Verbandsleben ein vorläufiges Ende. Aber schon sehr bald danach fanden sich die Stephaneer in der Bundesrepublik wieder zusammen und erweckten den Verband 1952 zu neuem Leben. Seitdem herrscht wieder ein reges Verbandsleben in allen Teilen des Landes. 45 Jahre lang gab es in Aschersleben keine Möglichkeit, die Tradition des VeSt. weiter zu pflegen, abgesehen von einigen Versuchen, zu jährlichen \"Traditionstreffen\" in den 50er Jahren. Die Wende brachte auch hier einen neuen Aufbruch. Seit dem Jahre 1992 werden die Jahrestreffen wieder regelmäßig in unserer Heimatstadt abgehalten."
					+ "Die Verbandszeitschrift \"Lose Blätter\" berichtet über die Tätigkeit der Gemeinschaft, über die Ergebnisse vielseitiger Forschungen und über das Leben der ehemaligen Mitschüler, damit die Erinnerung an Schule und Heimat erhalten bleibt. Ohne besondere Nachforschungen angestellt zu haben, kann man behaupten, dass unsere \"Ehemaligen-Zeitschrift\" zu den ältesten Deutschlands gehört. Sie hat alle Weltkriege und Umbrüche überstanden - manchmal mit einigen Schwierigkeiten - aber sie ist das einigende Band aller Stephaneer geblieben und so soll es bleiben."
					+ "In der 1991 zum 90. Jahrestag des Verbandes erschienenen Ausgabe der \"Losen Blätter\" lässt Dr. Paul - Wekel, dem in diesem Zusammenhang besonderer Dank für die Vervollständigung und Archivierung der Zeitschrift gilt, die Jahrzehnte - Revue passieren. Abschließend schrieb er \"So könnten wir stundenlang blättern. Immer ergibt sich etwas Interessantes, Wissenswertes, Erinnerungswürdiges. Gut, dass es \"Die losen Blätter\" gibt. Die gebundenen Jahrgänge sind in der Stadtbücherei auszuleihen."
					+ "Der Zweck des VeSt. dessen Zeichen die Fackel und die Eule sinnbildlich für Erleuchtung - und Weisheit stehen, ist die Pflege dieser Erinnerungen an Schule und Heimat, Erforschung der Geschichte unserer Heimatschulen sowie des Lebens und der Leistungen ihrer Schüler und Lehrer vom Mittelalter bis zur Gegenwart, Erkundung der künstlerischen, kulturellen und geschichtlichen Werte unserer Heimat, vor allem aber die Unterstützung der verehrten alten Schule durch Spenden aller Art. </div>"),
			new Beitrag("Der Vorstand stellt sich vor...", "<div style=\"text-align: justify;\">Menschenrechtserziehung, Friedenserziehung, Demokratielernen, Umweltbildung, interkulturelles Lernen, globales Lernen und Wissen um Weltkulturerbe sind die Kernthemen der UNESCO. Es waren die UNESCO-Schulen, die als Vorreiter diese Themen, die mittlerweile Allgemeingut sind, in der deutschen Schullandschaft überhaupt erst nachhaltig etabliert haben. UNESCO Schulen haben den Anspruch, für zukunftsweisende Themen und Arbeitsweisen Vorreiter zu sein, ohne sich dabei aber als Elite begreifen zu wollen. "
					+ "Zur Anerkennung als UNESCO-Projektschule gehört ein langjähriger mehrstufiger Prozess, der schließlich urkundlich durch die UNESCO-Zentrale in Paris bestätigt wird. Mit der Anerkennung wurde das Stephaneum Mitglied eines weltumspannenden Netzwerks von 9000 Schulen. "
					+ "Zur Mindestanforderung an den Erhalt des Titels gehören neben der Pflege von internationalen Kontakten die permanente und öffentliche Arbeit zu allen Kernthemen der UNESCO im schulischen Alltag. Neben unzähligen Workshops und Projekten konnte das Stephaneum u.a. einen Kontakt in den Oman aufbauen, kooperiert mit dem Gymnasium Lüchow, arbeitet zum Denkmalschutz und beteiligt sich regelmäßig an regionalen und bundesweiten Fachtagungen. Reserven bestehen hier vor allem noch in der Implementierung der Gedanken der UNESCO in den unterrichtlichen Alltag und dem Umgang mit aktuellen gesellschaftlichen Herausforderungen.  </div>")
	};
	
	//Untergruppen
	static private String[][] untergrp = {
			{
				"Aktuelles",
				"Schulprofil",
				"Ganztagsschule"
					},
			{
				"Verband"
					},
			{
				"Vorstand"
					},
	};
	
	//Gruppen
	static private String[] grp = {
			"Stephaneum",
			"V.e.St",
			"Förderkreis"
	};
	
	static public boolean createNutzer(int anzahl, int rang) {
		Konsole.method("Dummy.createNutzer("+anzahl+","+rang+")");
		
		Random randm = new Random();
		
		//Zugangscodes
		String[] codes = Datenbank.getAllZugangscodes(rang,false);
		if(codes.length < anzahl) {
			Konsole.warn("Zu wenige freie Zugangscodes.");
			return false;
		}
		
		//KlasseID
		int[] klasse_ids = null;
		if(rang == Nutzer.RANG_SCHUELER) {
			klasse_ids = Datenbank.getAllKlassenID();
		}
		
		
		//Generierung und Registrierung der Nutzer
		int vorname_index;
		String vorname;
		String nachname;
		String email;
		int klasse_id;
		
		StringBuilder stringBuilder = new StringBuilder();
		
		Datenbank.prepareRegisterNutzerFast();
		for(int i = 0; i < anzahl; i++) {
			
			if(rang == Nutzer.RANG_SCHUELER)
				klasse_id = klasse_ids[randm.nextInt(klasse_ids.length)];
			else
				klasse_id = -1;
			vorname_index = randm.nextInt(namen.length);
			vorname = namen[vorname_index][0];
			nachname = namen[randm.nextInt(namen.length)][1];
			
			//Email
			stringBuilder.setLength(0); //vorheriges löschen
			stringBuilder.append(vorname);
			stringBuilder.append('.');
			stringBuilder.append(nachname);
			stringBuilder.append(email_domain[randm.nextInt(email_domain.length)]);
			email = stringBuilder.toString().toLowerCase();
			
			//Alle Endinformationen zur Datenbank-Funktion schicken
			Datenbank.registerNutzerFast(codes[i], klasse_id, vorname, nachname, geschlecht[vorname_index], email, vorname+nachname);
		}
		
		return true;
	}
	
	//0... OK, 1... min. 2 Nutzer!, 2... zu wenig Nutzer registriert
	static public int createProjekt(int anzahl, int min_nutzer_pro_projekt, int max_nutzer_pro_projekt) {
		Konsole.method("Dummy.createProjekt("+anzahl+","+min_nutzer_pro_projekt+","+max_nutzer_pro_projekt+")");
		
		
		if(min_nutzer_pro_projekt < 2) {
			return 1;
		}
		
		
		Random randm = new Random();
		int[] nutzer_id = Datenbank.getAllNutzerID(0);
		
		if(nutzer_id.length < max_nutzer_pro_projekt) {
			return 2;
		}
		
		for(int i = 0; i < anzahl; i++) {
			//Neues Projekt erstellen
			boolean[] used = new boolean[nutzer_id.length]; //Redundanz vermeiden
			
			int index_leiter = randm.nextInt(nutzer_id.length); //index-leiter zufällig auswählen
			int leiter = nutzer_id[index_leiter]; //leiter-nutzer-ID
			used[index_leiter] = true;
			Datenbank.createProjekt(projekt_name[randm.nextInt(projekt_name.length)],leiter);
			int latest_projekt_id = Datenbank.getNextProjektID()-1;
			
			//Neue Nutzer zum Projekt hinzufügen
			int anzahl_nutzer = min_nutzer_pro_projekt; 
			if(max_nutzer_pro_projekt > min_nutzer_pro_projekt)
				anzahl_nutzer = randm.nextInt(max_nutzer_pro_projekt-min_nutzer_pro_projekt+1)+min_nutzer_pro_projekt;
			
			anzahl_nutzer--; //weil leiter mitzählt
			
			for(int x = 0; x < anzahl_nutzer; x++) {
				int index_neuer_nutzer = 0;
				int neuer_nutzer = 0;
				do {
					index_neuer_nutzer = randm.nextInt(nutzer_id.length);
				}while(used[index_neuer_nutzer] == true); //bis ein nicht-bereits-im-projekt-befindlicher-nutzer gefunden wird
				used[index_neuer_nutzer] = true;
				neuer_nutzer = nutzer_id[index_neuer_nutzer];
				Datenbank.joinProjekt(neuer_nutzer, latest_projekt_id, false);
			}
		}
		
		return 0;
	}
	
	static public void createBeitrag(int min_anzahl_pro_untergruppe, int max_anzahl_pro_untergruppe) {
		Random randm = new Random();
		Datei[] bilder = Datenbank.getDateiArrayAllImages(Sitzung.getNutzer().getNutzer_id());
		
		//Obergruppen
		for(int x = 0; x < grp.length; x++) {
			Datenbank.createGruppe(-1, grp[x],grp.length-x, null,null);
			int grpID = Datenbank.getNextGruppeID()-1;
			
			//Untergruppen
			for(int y = 0; y < untergrp[x].length; y++) {
				Datenbank.createGruppe(grpID, untergrp[x][y],untergrp[x].length-y,null,null);
				int untergrpID = Datenbank.getNextGruppeID()-1;
				
				//in dieser Untergruppe beliebig viele Beiträge erstellen
				int anzahl = max_anzahl_pro_untergruppe;
				if(max_anzahl_pro_untergruppe > min_anzahl_pro_untergruppe)
					anzahl = randm.nextInt(max_anzahl_pro_untergruppe-min_anzahl_pro_untergruppe)+min_anzahl_pro_untergruppe;
				
				for(int z = 0; z < anzahl; z++) {
					int beitrag_index = randm.nextInt(beitrag.length);
					int beitrag_id = Datenbank.createBeitrag(untergrpID, 1, beitrag[beitrag_index].getTitel(), beitrag[beitrag_index].getText(), 400, true); //Beitrag erstellen
					
					if(bilder.length > 0) { //Hat der Nutzer Bilder?
						int anzahl_bilder = beitrag[beitrag_index].getText().length()/750; //ein zufälliges Bild hinzufügen (pro 750 Zeichen)
						for(int a = 0; a < anzahl_bilder; a++) {
							int datei_id = bilder[randm.nextInt(bilder.length)].getDatei_id();
							Datenbank.connectDateiBeitrag(datei_id, beitrag_id); 
						}
					}
				}
			}
		}
	}

}