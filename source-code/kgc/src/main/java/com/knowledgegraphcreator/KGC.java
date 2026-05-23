package com.knowledgegraphcreator;

import com.markwatson.ner_dbpedia.TextToDbpediaUris;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Java implementation of Knowledge Graph Creator.
 *
 * Copyright 2020 Mark Watson. All Rights Reserved. Apache 2 license.
 *
 * For documentation see my book "Practical Artificial Intelligence Programming
 * With Java", chapter "Automatically Generating Data for Knowledge Graphs"
 * at https://leanpub.com/javaai that can be read free online.
 *
 */

public class KGC  {

	private static final System.Logger LOG = System.getLogger(KGC.class.getName());

	private static final String SUBJECT_URI = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#/subject>";
	private static final String LABEL_URI = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#/label>";
	private static final String COUNTRY_TYPE_URI = "<http://www.w3.org/2000/01/rdf-schema#country>";
	private static final String PERSON_TYPE_URI = "<http://www.w3.org/2000/01/rdf-schema#person>";
	private static final String COMPANY_TYPE_URI = "<http://www.w3.org/2000/01/rdf-schema#company>";
	private static final String CITY_TYPE_URI = "<http://www.w3.org/2000/01/rdf-schema#city>";
	private static final String BROADCAST_NETWORK_TYPE_URI = "<http://www.w3.org/2000/01/rdf-schema#broadcastNetwork>";
	private static final String MUSIC_GROUP_TYPE_URI = "<http://www.w3.org/2000/01/rdf-schema#musicGroup>";
	private static final String POLITICAL_PARTY_TYPE_URI = "<http://www.w3.org/2000/01/rdf-schema#politicalParty>";
	private static final String TRADE_UNION_TYPE_URI = "<http://www.w3.org/2000/01/rdf-schema#tradeUnion>";
	private static final String UNIVERSITY_TYPE_URI = "<http://www.w3.org/2000/01/rdf-schema#university>";
	private static final String TYPE_OF_URI = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";

	/** Immutable holder for a text file and its associated metadata. */
	private record TextAndMeta(String text, String meta) {}

	private KGC() { }

	public KGC(String directoryPath, String outputRdfPath) throws IOException {
		process(directoryPath, outputRdfPath);
	}

	/**
	 * Process all .txt/.meta file pairs in {@code directoryPath} and write
	 * RDF triples to {@code outputRdfPath}.
	 */
	public static void process(String directoryPath, String outputRdfPath) throws IOException {
		LOG.log(System.Logger.Level.INFO, "KGC processing directory: {0}", directoryPath);
		Path dirPath = Path.of(directoryPath);
		File[] directoryListing = dirPath.toFile().listFiles();
		if (directoryListing == null) {
			LOG.log(System.Logger.Level.WARNING, "Directory listing returned null for: {0}", directoryPath);
			return;
		}

		try (var out = new PrintStream(outputRdfPath)) {
			for (File child : directoryListing) {
				if (!child.toString().endsWith(".txt")) {
					continue;
				}
				LOG.log(System.Logger.Level.DEBUG, "Processing file: {0}", child);

				// try to open the meta file with the same extension:
				String metaAbsolutePath = child.getAbsolutePath();
				Path metaPath = Path.of(metaAbsolutePath.substring(0, metaAbsolutePath.length() - 4) + ".meta");
				LOG.log(System.Logger.Level.DEBUG, "Meta file: {0}", metaPath);

				TextAndMeta data = readData(child.toPath(), metaPath);
				String metaData = "<" + data.meta().strip() + ">";
				TextToDbpediaUris kt = new TextToDbpediaUris(data.text());

				writeTriples(out, metaData, kt.personNames, kt.personUris, PERSON_TYPE_URI);
				writeTriples(out, metaData, kt.companyNames, kt.companyUris, COMPANY_TYPE_URI);
				writeTriples(out, metaData, kt.cityNames, kt.cityUris, CITY_TYPE_URI);
				writeTriples(out, metaData, kt.countryNames, kt.countryUris, COUNTRY_TYPE_URI);
				writeTriples(out, metaData, kt.broadcastNetworkNames, kt.broadcastNetworkUris, BROADCAST_NETWORK_TYPE_URI);
				writeTriples(out, metaData, kt.musicGroupNames, kt.musicGroupUris, MUSIC_GROUP_TYPE_URI);
				writeTriples(out, metaData, kt.politicalPartyNames, kt.politicalPartyUris, POLITICAL_PARTY_TYPE_URI);
				writeTriples(out, metaData, kt.tradeUnionNames, kt.tradeUnionUris, TRADE_UNION_TYPE_URI);
				writeTriples(out, metaData, kt.universityNames, kt.universityUris, UNIVERSITY_TYPE_URI);
			}
		}
	}

	/**
	 * Write subject, label, and type triples for a list of named entities.
	 */
	private static void writeTriples(PrintStream out, String metaData,
			List<String> names, List<String> uris, String typeUri) {
		for (int i = 0; i < names.size(); i++) {
			out.println(metaData + " " + SUBJECT_URI + " " + uris.get(i) + " .");
			out.println(uris.get(i) + " " + LABEL_URI + " \"" + names.get(i) + "\" .");
			out.println(uris.get(i) + " " + TYPE_OF_URI + " " + typeUri + " .");
		}
	}

	private static TextAndMeta readData(Path textPath, Path metaPath) throws IOException {
		String text = Files.readString(textPath, StandardCharsets.UTF_8);
		String meta = Files.readString(metaPath, StandardCharsets.UTF_8);
		LOG.log(System.Logger.Level.DEBUG, "Read text ({0} chars) from {1}", text.length(), textPath);
		return new TextAndMeta(text, meta);
	}

}
