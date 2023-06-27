package org.eurocris.openaire.cris.validator;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.cli.MissingArgumentException;
import org.eurocris.openaire.cris.validator.OAIPMHEndpoint.ConnectionStreamFactory;
import org.eurocris.openaire.cris.validator.tree.CERIFNode;
import org.eurocris.openaire.cris.validator.util.CheckingIterable;
import org.eurocris.openaire.cris.validator.util.FileSavingInputStream;
import org.eurocris.openaire.cris.validator.util.XmlUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runners.MethodSorters;
import org.openarchives.oai._2.DescriptionType;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.MetadataFormatType;
import org.openarchives.oai._2.MetadataType;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2.SetType;
import org.openarchives.oai._2.StatusType;
import org.openarchives.oai._2_0.oai_identifier.OaiIdentifierType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validating a given OAI-PMH endpoint for compliance with the OpenAIRE Guidelines for CRIS Managers 1.1.
 * This is organized as a JUnit4 test suite that works over a given OAI-PMH endpoint. 
 * @author jdvorak001
 * @see <a href="https://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/">the text of the specification</a>
 * @see <a href="https://github.com/openaire/guidelines-cris-managers">the github project of the specification, XML Schema and examples</a>
 * @see <a href="https://github.com/jdvorak001/openaire-cris-validator">this project on GitHub</a>
 */
@FixMethodOrder( value=MethodSorters.NAME_ASCENDING )
public class CRISValidator {
	
	/**
	 * The spec of the set of equipments.
	 */
	public static final String OPENAIRE_CRIS_EQUIPMENTS__SET_SPEC = "openaire_cris_equipments";
	/**
	 * The spec of the set of events.
	 */
	public static final String OPENAIRE_CRIS_EVENTS__SET_SPEC = "openaire_cris_events";
	/**
	 * The spec of the set of fundings.
	 */
	public static final String OPENAIRE_CRIS_FUNDING__SET_SPEC = "openaire_cris_funding";
	/**
	 * The spec of the set of projects.
	 */
	public static final String OPENAIRE_CRIS_PROJECTS__SET_SPEC = "openaire_cris_projects";
	/**
	 * The spec of the set of organisation units.
	 */
	public static final String OPENAIRE_CRIS_ORGUNITS__SET_SPEC = "openaire_cris_orgunits";
	/**
	 * The spec of the set of persons.
	 */
	public static final String OPENAIRE_CRIS_PERSONS__SET_SPEC = "openaire_cris_persons";
	/**
	 * The spec of the set of patents.
	 */
	public static final String OPENAIRE_CRIS_PATENTS__SET_SPEC = "openaire_cris_patents";
	/**
	 * The spec of the set of products.
	 */
	public static final String OPENAIRE_CRIS_PRODUCTS__SET_SPEC = "openaire_cris_products";
	/**
	 * The spec of the set of publications.
	 */
	public static final String OPENAIRE_CRIS_PUBLICATIONS__SET_SPEC = "openaire_cris_publications";
	
	/**
	 * The OAI-PMH metadata prefix for the CERIF XML format.
	 */
	public static final String OAI_CERIF_OPENAIRE__METADATA_PREFIX = "oai_cerif_openaire";
	
	/**
	 * The URI of the XML namespace.
	 */
	public static final String OPENAIRE_CERIF_XMLNS_PREFIX = "https://www.openaire.eu/cerif-profile/";
	
	/**
	 * The URL base for the XML Schema locations by version.
	 */
	public static final String OPENAIRE_CERIF_SCHEMAS_ROOT = "https://www.openaire.eu/schema/cris/";
	
	/**
	 * The URL base for the XML Schema location of the current version.
	 */
	public static final String CURRENT_XML_SCHEMA_URL_PREFIX = OPENAIRE_CERIF_SCHEMAS_ROOT + "current/";

	/**
	 * The name of the XML Schema file. 
	 */
	public static final String OPENAIRE_CERIF_SCHEMA_FILENAME = "openaire-cerif-profile.xsd";
	
	/**
	 * The connection stream factory to use for getting the response stream from a connection.
	 */
	public static final ConnectionStreamFactory CONN_STREAM_FACTORY = new FileLoggingConnectionStreamFactory( "data" );
	
	private static final Map<String, MetadataFormatType> metadataFormatsByPrefix = new HashMap<>();
	
	private static final Map<String, String> schemaUrlsByNs = new HashMap<>();
	private static final Map<String, String> nssBySchemaUrl = new HashMap<>();
	
	/**
	 * The main method: used for running the JUnit4 test suite from the command line.
	 * The first command line argument should be the URL of the endpoint to test.
	 * @param args command line arguments
	 * @throws Exception any uncaught exception 
	 */
	public static void main( final String[] args ) throws Exception {
		final String endpointUrl = ( args.length > 0 ) ? args[0] : null;
		final URL endpointBaseUrl = URI.create( endpointUrl ).toURL();
		endpoint = new OAIPMHEndpoint( endpointBaseUrl, getParserSchema(), CONN_STREAM_FACTORY );
		JUnitCore.main( CRISValidator.class.getName() );
	}
	
	private static OAIPMHEndpoint endpoint;

	/**
	 * Set up the test suite.
	 * @throws MissingArgumentException when the endpoint URL is not specified
	 * @throws SAXException when the parser schema cannot be created
	 * @throws IOException on a problem accessing a schema
	 * @throws ParserConfigurationException when an XML parser cannot be instantiated
	 */
	public CRISValidator() throws MissingArgumentException, SAXException, IOException, ParserConfigurationException {
		if ( endpoint == null ) {
			final String endpointPropertyKey = "endpoint.to.validate";
			final String endpointUrl = System.getProperty( endpointPropertyKey );
			if ( endpointUrl == null ) {
				throw new MissingArgumentException( "Please specify the OAI-PMH endpoint URL as the value of the " + endpointPropertyKey + " system property or as the first argument on the command line" );
			}
			final URL endpointBaseUrl = URI.create( endpointUrl ).toURL();
			endpoint = new OAIPMHEndpoint( endpointBaseUrl, getParserSchema(), CONN_STREAM_FACTORY );			
		}
	}
	
	/**
	 * @return the URL of the endpoint
	 */
	public String getName() {
		return endpoint.getBaseUrl();
	}
	
	private static Schema getSchema( final StreamSource ... sources ) throws IOException, SAXException, ParserConfigurationException {
		final List<Source> schemaList = new ArrayList<>();
		for ( final StreamSource source : sources ) {
			schemaList.add( source );
			final String schemaUrl = source.getSystemId();
			final Document doc = getDocumentBuilderFactory().newDocumentBuilder().parse( source.getInputStream() );
			final String targetNamespace = doc.getDocumentElement().getAttribute( "targetNamespace" );
			schemaUrlsByNs.put( targetNamespace, schemaUrl );
			nssBySchemaUrl.put( schemaUrl, targetNamespace );
			System.out.println( "Will use " + schemaUrl + " for namespace " + targetNamespace );
		}
		return getXmlSchemaFactory().newSchema( schemaList.toArray( new Source[0] ) );
	}

	private static Schema parserSchema = null;
	
	/**
	 * Create the schema for the validating XML parser.
	 * @return the compound schema
	 * @throws SAXException on a problem reading the schema
	 * @throws IOException on a problem accessing the schema
	 * @throws ParserConfigurationException 
	 */
	protected static synchronized Schema getParserSchema() throws SAXException, IOException, ParserConfigurationException {
		if ( parserSchema == null ) {
			parserSchema = getSchema(
				schema( "/cached/xml.xsd", "http://www.w3.org/2001/xml.xsd" ),
				schema( "/cached/oai-identifier.xsd" ),
				schema( "/cached/simpledc20021212.xsd", "http://dublincore.org/schemas/xmls/simpledc20021212.xsd" ), 
				schema( "/cached/oai_dc.xsd" ),
				schema( "/cached/provenance.xsd", "http://www.openarchives.org/OAI/2.0/provenance.xsd" ),
				schema( "/cached/OAI-PMH.xsd" ),
				schema( "/current/openaire-cerif-profile.xsd", "https://www.openaire.eu/schema/cris/1.2/openaire-cerif-profile.xsd" ),
				schema( "/cerif_profile_1_1/openaire-cerif-profile.xsd", "https://www.openaire.eu/schema/cris/1.1.1/openaire-cerif-profile.xsd" )
			);
		}
		return parserSchema;
	}

	private static SchemaFactory xmlSchemaFactory = null;
	
	private static synchronized SchemaFactory getXmlSchemaFactory() {
		if ( xmlSchemaFactory == null ) {
			xmlSchemaFactory = SchemaFactory.newInstance( W3C_XML_SCHEMA_NS_URI );
		}
		return xmlSchemaFactory;
	}
	
	private static StreamSource schema( final String path ) {
		return schema( path, null );
	}
	
	private static StreamSource schema( final String path, final String externalUrl ) {
		final String path1 = "/schemas" + path;
		final URL url = OAIPMHEndpoint.class.getResource( path1 );
		if ( url == null ) {
			throw new IllegalArgumentException( "Resource " + path1 + " not found" );
		}
		return new StreamSource( (InputStream) null, url.toExternalForm() ) {
			public InputStream getInputStream() {
				return OAIPMHEndpoint.class.getResourceAsStream( path1 );
			}
		};
	}

	@SuppressWarnings( "unused" )
	private static Optional<String> sampleIdentifier = Optional.empty();

	private static Optional<String> serviceAcronym = Optional.empty();
	
	/**
	 * Ask for ?verb=Identity and test it for consistence – checks (1).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check000_Identify() throws Exception {
		final IdentifyType identify = endpoint.callIdentify();
		CheckingIterable<DescriptionType> checker = CheckingIterable.over( identify.getDescription() );
		checker = checker.checkContainsOne( new Predicate<DescriptionType>() {

			@Override
			public boolean test( final DescriptionType description ) {
				final Object obj = description.getAny();
				if ( obj instanceof JAXBElement<?> ) {
					final JAXBElement<?> jaxbEl = (JAXBElement<?>) obj;
					final Object obj1 = jaxbEl.getValue();
					if ( obj1 instanceof OaiIdentifierType ) {
						final OaiIdentifierType oaiIdentifier = (OaiIdentifierType) obj1;
						sampleIdentifier = Optional.ofNullable( oaiIdentifier.getSampleIdentifier() );
						return true;
					}
				}
				return false;
			}
			
		}, "the Identify descriptions list (1b)", "an 'oai-identifier' element" );
		checker = checker.checkContainsOne( new Predicate<DescriptionType>() {

			@Override
			public boolean test( final DescriptionType description ) {
				final Object obj = description.getAny();
				if ( obj instanceof Element ) {
					final Element el = (Element) obj;
					if ( "Service".equals( el.getLocalName() ) && el.getNamespaceURI() != null && el.getNamespaceURI().startsWith( OPENAIRE_CERIF_XMLNS_PREFIX ) ) {
						serviceAcronym = XmlUtils.getTextContents( XmlUtils.getFirstMatchingChild( el, "Acronym", el.getNamespaceURI() ) );
						validateMetadataPayload( el );
						return true;
					}
				}
				return false;
			}
			
		}, "the Identify descriptions list (1a)", "a 'Service' element" );
		checker.run();
		if ( ! endpoint.getBaseUrl().startsWith( "file:" ) ) {
			assertEquals( "Identify response has a different endpoint base URL (1d)", endpoint.getBaseUrl(), identify.getBaseURL() );
		}
		final Optional<String> repoIdentifier = endpoint.getRepositoryIdentifer();
		if ( serviceAcronym.isPresent() && repoIdentifier.isPresent() ) {
			assertEquals( "Service acronym is not the same as the repository identifier (1c)", serviceAcronym.get(), repoIdentifier.get() );
		}
	}
	
	/**
	 * Ask for ?verb=ListMetadataFormats and test it for consistence – checks (2).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check010_MetadataFormats() throws Exception {
		CheckingIterable<MetadataFormatType> checker = CheckingIterable.over( endpoint.callListMetadataFormats().getMetadataFormat() );
		checker = checker.checkUnique( MetadataFormatType::getMetadataPrefix, "Metadata prefix not unique" );
		checker = checker.checkUnique( MetadataFormatType::getMetadataNamespace, "Metadata namespace not unique" );
		checker = checker.checkUnique( MetadataFormatType::getSchema, "Metadata schema location not unique" );
		checker = wrapCheckMetadataFormatPresent( checker );
		checker = checker.map( (MetadataFormatType mft) -> { 
			final String prefix = mft.getMetadataPrefix();
			if ( prefix.startsWith(OAI_CERIF_OPENAIRE__METADATA_PREFIX) ) { 
				metadataFormatsByPrefix.put( prefix, mft );
			}
			return mft; 
		} );
		final long nMetadataFormats = checker.run();
		System.out.println( "Having " + metadataFormatsByPrefix.size() + " OpenAIRE CRIS metadata formats (out of the total " + nMetadataFormats + " metadata formats)" );
	}
	
	private CheckingIterable<MetadataFormatType> wrapCheckMetadataFormatPresent( final CheckingIterable<MetadataFormatType> parent ) {
		final Predicate<MetadataFormatType> predicate = new Predicate<MetadataFormatType>() {

			@Override
			public boolean test( final MetadataFormatType mf ) {
				final String metadataNs = mf.getMetadataNamespace();
				if ( metadataNs.startsWith( OPENAIRE_CERIF_XMLNS_PREFIX ) ) {
					assertTrue( "The metadata prefix of an OpenAIRE CRIS Guidelines namespace shall start with " + OAI_CERIF_OPENAIRE__METADATA_PREFIX, mf.getMetadataPrefix().startsWith(OAI_CERIF_OPENAIRE__METADATA_PREFIX) );
					try {
						final DocumentBuilder db = getDocumentBuilderFactory().newDocumentBuilder();
						final String schemaUrl = mf.getSchema();
						System.out.println( "Metadata format prefix " + mf.getMetadataPrefix() + " with ns " + mf.getMetadataNamespace() );
						assertTrue( "Please reference the official XML Schema at " + OPENAIRE_CERIF_SCHEMAS_ROOT + " (2)", schemaUrl.startsWith( OPENAIRE_CERIF_SCHEMAS_ROOT ) );
						assertTrue( "The schema file should be " + OPENAIRE_CERIF_SCHEMA_FILENAME + " (2)", schemaUrl.endsWith( "/" + OPENAIRE_CERIF_SCHEMA_FILENAME ) );
						final String localSchemaUrl = schemaUrlsByNs.get( metadataNs );
						assertNotNull( "This validator shall cover the metadata namespace " + metadataNs, localSchemaUrl );
						if ( !localSchemaUrl.contains( "/current/" ) ) {
							final Document doc = db.parse( localSchemaUrl );
							final Element schemaRootEl = doc.getDocumentElement();
							final String targetNsUri = schemaRootEl.getAttribute( "targetNamespace" );
							assertEquals( "The schema does not have the advertised target namespace URI (2)", metadataNs, targetNsUri );
						}
					} catch ( final ParserConfigurationException | SAXException | IOException e ) {
						throw new IllegalStateException( e ); 
					}
					return true;
				}
				return false;
			}

		};
		return parent.checkContains( predicate, new AssertionError( "MetadataFormat for the OpenAIRE Guidelines for CRIS Managers not present (2)" ) );
	}
	
	/**
	 * Get a {@link DocumentBuilderFactory} for parsing.
	 * @return
	 */
	protected static DocumentBuilderFactory getDocumentBuilderFactory() {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware( true );
		dbf.setValidating( false );
		dbf.setIgnoringComments( true );
		return dbf;
	}

	/**
	 * Ask for ?verb=ListSets and test it for consistence – checks (3).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check020_Sets() throws Exception {
		CheckingIterable<SetType> checker = CheckingIterable.over( endpoint.callListSets() );
		checker = checker.checkUnique( SetType::getSetSpec, "setSpec not unique" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_PUBLICATIONS__SET_SPEC, "OpenAIRE_CRIS_publications" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_PRODUCTS__SET_SPEC, "OpenAIRE_CRIS_products" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_PATENTS__SET_SPEC, "OpenAIRE_CRIS_patents" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_PERSONS__SET_SPEC, "OpenAIRE_CRIS_persons" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_ORGUNITS__SET_SPEC, "OpenAIRE_CRIS_orgunits" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_PROJECTS__SET_SPEC, "OpenAIRE_CRIS_projects" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_FUNDING__SET_SPEC, "OpenAIRE_CRIS_funding" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_EVENTS__SET_SPEC, "OpenAIRE_CRIS_events" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_EQUIPMENTS__SET_SPEC, "OpenAIRE_CRIS_equipments" );
		checker.run();
	}
	
	private CheckingIterable<SetType> wrapCheckSetPresent( final CheckingIterable<SetType> parent, final String expectedSetSpec, final String expectedSetName ) {
		final Predicate<SetType> predicate = new Predicate<SetType>() {

			@Override
			public boolean test( final SetType s ) {
				if ( expectedSetSpec.equals( s.getSetSpec() ) ) {
					assertEquals( "Non-matching set name for set '" + expectedSetSpec + "' (3)", expectedSetName, s.getSetName() );
					return true;
				}
				return false;
			}

		};
		return parent.checkContains( predicate, new AssertionError( "Set '" + expectedSetSpec + "' not present (3)" ) );
	}

	/**
	 * Ask for ?verb=ListRecords on the products set and test it for consistence – checks (5).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check100_CheckPublications() throws Exception {
		for ( final String prefix : metadataFormatsByPrefix.keySet() ) {
			final Iterable<RecordType> records = endpoint.callListRecords( prefix, OPENAIRE_CRIS_PUBLICATIONS__SET_SPEC, null, null );
			final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records, "Publication" );
			checker.run();
		}
	}

	/**
	 * Ask for ?verb=ListRecords on the publications set and test it for consistence – checks (5).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check200_CheckProducts() throws Exception {
		for ( final String prefix : metadataFormatsByPrefix.keySet() ) {
			final Iterable<RecordType> records = endpoint.callListRecords( prefix, OPENAIRE_CRIS_PRODUCTS__SET_SPEC, null, null );
			final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records, "Product" );
			checker.run();
		}
	}

	/**
	 * Ask for ?verb=ListRecords on the patents set and test it for consistence – checks (5).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check300_CheckPatents() throws Exception {
		for ( final String prefix : metadataFormatsByPrefix.keySet() ) {
			final Iterable<RecordType> records = endpoint.callListRecords( prefix, OPENAIRE_CRIS_PATENTS__SET_SPEC, null, null );
			final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records, "Patent" );
			checker.run();
		}
	}

	/**
	 * Ask for ?verb=ListRecords on the persons set and test it for consistence – checks (5).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check400_CheckPersons() throws Exception {
		for ( final String prefix : metadataFormatsByPrefix.keySet() ) {
			final Iterable<RecordType> records = endpoint.callListRecords( prefix, OPENAIRE_CRIS_PERSONS__SET_SPEC, null, null );
			final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records, "Person" );
			checker.run();
		}
	}

	/**
	 * Ask for ?verb=ListRecords on the organisation units set and test it for consistence – checks (5).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check500_CheckOrgUnits() throws Exception {
		for ( final String prefix : metadataFormatsByPrefix.keySet() ) {
			final Iterable<RecordType> records = endpoint.callListRecords( prefix, OPENAIRE_CRIS_ORGUNITS__SET_SPEC, null, null );
			final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records, "OrgUnit" );
			checker.run();
		}
	}

	/**
	 * Ask for ?verb=ListRecords on the projects set and test it for consistence – checks (5).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check600_CheckProjects() throws Exception {
		for ( final String prefix : metadataFormatsByPrefix.keySet() ) {
			final Iterable<RecordType> records = endpoint.callListRecords( prefix, OPENAIRE_CRIS_PROJECTS__SET_SPEC, null, null );
			final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records, "Project" );
			checker.run();
		}
	}

	/**
	 * Ask for ?verb=ListRecords on the fundings set and test it for consistence – checks (5).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check700_CheckFundings() throws Exception {
		for ( final String prefix : metadataFormatsByPrefix.keySet() ) {
			final Iterable<RecordType> records = endpoint.callListRecords( prefix, OPENAIRE_CRIS_FUNDING__SET_SPEC, null, null );
			final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records, "Funding" );
			checker.run();
		}
	}

	/**
	 * Ask for ?verb=ListRecords on the equipment set and test it for consistence – checks (5).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check800_CheckEquipment() throws Exception {
		for ( final String prefix : metadataFormatsByPrefix.keySet() ) {
			final Iterable<RecordType> records = endpoint.callListRecords( prefix, OPENAIRE_CRIS_EQUIPMENTS__SET_SPEC, null, null );
			final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records, "Equipment" );
			checker.run();
		}
	}

	/**
	 * Ask for ?verb=ListRecords on the events set and test it for consistence – checks (5).
	 * @throws Exception on any unexpected circumstance
	 */
	@Test
	public void check900_CheckEvents() throws Exception {
		for ( final String prefix : metadataFormatsByPrefix.keySet() ) {
			final Iterable<RecordType> records = endpoint.callListRecords( prefix, OPENAIRE_CRIS_EVENTS__SET_SPEC, null, null );
			final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records, "Event" );
			checker.run();
		}
	}

	/**
	 * Prepare the checks to run on all CERIF records. 
	 * @param records the iterable containing the records
	 * @param expectedElementLocalName the expected local name of the metadata elements
	 * @return a {@link CheckingIterable} that checks the namespace, the uniqueness of the OAI identifiers of the records and their consistence with the types and IDs of the objects
	 */
	protected CheckingIterable<RecordType> buildCommonCheckersChain( final Iterable<RecordType> records, final String expectedElementLocalName ) {
		return wrapCheckPayloadQNameAndAccummulate( expectedElementLocalName,
				wrapCheckUniqueness( 
						wrapCheckOAIIdentifier( 
								CheckingIterable.over( records ) 
						) 
				) 
		);
	}
	
	private CheckingIterable<RecordType> wrapCheckUniqueness( final CheckingIterable<RecordType> checker ) {
		final Function<RecordType, HeaderType> f1 = RecordType::getHeader;
		return checker.checkUnique( f1.andThen( HeaderType::getIdentifier ), "record identifier not unique" );
	}
	
	private CheckingIterable<RecordType> wrapCheckOAIIdentifier( final CheckingIterable<RecordType> checker ) {
		final Optional<String> repoIdentifier = endpoint.getRepositoryIdentifer();
		if ( repoIdentifier.isPresent() ) {
			final Function<RecordType, Set<String>> expectedFunction = new Function<RecordType, Set<String>>() {
				
				@Override
				public Set<String> apply( final RecordType x ) {
					final MetadataType metadata = x.getMetadata();
					final Set<String> results = new HashSet<>();
					if ( metadata != null ) {
						final Element el = (Element) metadata.getAny();
						final String id = el.getAttribute( "id" );
						results.add("oai:" + repoIdentifier.get() + ":" + el.getLocalName() + "s/" + id);
						results.add("oai:" + repoIdentifier.get() + ":" + id);
					} else {
						// make the test trivially satisfied for records with no metadata
					    results.add(x.getHeader().getIdentifier());
					}
					return results;
				}

			};
			return checker.checkForAllValueInSet( expectedFunction, ( ( final RecordType record ) -> record.getHeader().getIdentifier() ), "OAI identifier other than expected" );
		} else {
			return checker;
		}
	}

	private static Map<String, CERIFNode> recordsByName = new HashMap<>();
	private static Map<String, CERIFNode> recordsByOaiIdentifier = new HashMap<>();
	
	private CheckingIterable<RecordType> wrapCheckPayloadQNameAndAccummulate( final String expectedElementLocalName, final CheckingIterable<RecordType> checker ) {
		return checker.checkForAll( new Predicate<RecordType>() {

			@Override
			public boolean test( final RecordType t ) {
				final MetadataType recordMetadata = t.getMetadata();
				if ( recordMetadata != null ) {
					final Object obj = recordMetadata.getAny();
					if ( obj instanceof Element ) {
						final Element el = (Element) obj;
						assertTrue( "The payload element not in the right namespace", el.getNamespaceURI().startsWith(OPENAIRE_CERIF_XMLNS_PREFIX) );
						assertEquals( "The payload element does not have the right local name", expectedElementLocalName, el.getLocalName() );
						validateMetadataPayload( el );
						final CERIFNode node = CERIFNode.buildTree( el );
						recordsByName.put( node.getName(), node );
						recordsByOaiIdentifier.put( t.getHeader().getIdentifier(), node );
						return true;
					}
				}
				// fail unless the record is deleted
				return StatusType.DELETED.equals( t.getHeader().getStatus() );
			}
			
		}, "Metadata missing from OAI-PMH record" );
	}
	
	private static final String[] types = new String[] { "Publication", "Product", "Patent", "Person", "OrgUnit", "Project", "Funding", "Event", "Equipment" };
	static {
		Arrays.sort( types );
	}
	
	/**
	 * Test the accummulated data for consistence – checks (5a) and (5b).
	 */
	@Test
	public void check990_CheckReferentialIntegrityAndFunctionalDependency() {
		for ( final Map.Entry<String, CERIFNode> entry : recordsByOaiIdentifier.entrySet() ) {
			final String oaiIdentifier = entry.getKey();
			final CERIFNode node = entry.getValue();
			// for all harvested CERIF data, check the children of the main objects (no need to check the objects themselves, they satisfy all checks trivially)
			for ( final CERIFNode node3 : node.getChildren( null ) ) {
				lookForCERIFObjectsAndCheckReferentialIntegrityAndFunctionalDependency( node3, oaiIdentifier );
			}
		}
	}

	private void lookForCERIFObjectsAndCheckReferentialIntegrityAndFunctionalDependency( final CERIFNode node, final String oaiIdentifier ) {
		// do the checks if this is a CERIF object
		final String type = node.getType();
		if ( Arrays.binarySearch( types, type ) >= 0 ) {
			doCheckFunctionalDependency( node, oaiIdentifier );
		}
		// recurse to children of this node
		for ( final CERIFNode node2 : node.getChildren( null ) ) {
			lookForCERIFObjectsAndCheckReferentialIntegrityAndFunctionalDependency( node2, oaiIdentifier );
		}
	}

	private void doCheckFunctionalDependency( final CERIFNode node, final String oaiIdentifier ) {
		final String name = node.getName();
		if ( name.contains( "[@id=\"" ) ) {
			final CERIFNode baseNode = recordsByName.get( name );
			assertNotNull( "Record for " + name + " not found, referential integrity violated in " + oaiIdentifier + " (5a)", baseNode );
			if ( ! node.isSubsetOf( baseNode ) ) {
				final CERIFNode missingNode = node.reportWhatIMiss( baseNode ).get();
				fail( "Violation of (5b) in " + oaiIdentifier + ":\n" + node + "is not subset of\n" + baseNode + "missing is\n" + missingNode );
			}
		}
	}

	/**
	 * Validate the metadata payload subtree against the XML Schema resulting from {@link #getValidatorSchema()}.
	 * @param el the metadata payload top element
	 */
	protected void validateMetadataPayload( final Element el ) {
		final String elString = el.getLocalName() + "[@id=\"" + el.getAttribute( "id" ) + "\"]";
		try {
			final Validator validator = getParserSchema().newValidator();
			final ErrorHandler errorHandler = new ErrorHandler() {
				
				private boolean patternValidErrorSignalled = false;
				
				@Override
				public void warning( final SAXParseException exception ) throws SAXException {
					// do nothing
				}
				
				@Override
				public void fatalError( final SAXParseException exception ) throws SAXException {
					throw exception;
				}
				
				@Override
				public void error( final SAXParseException exception ) throws SAXException {
					final String msg = exception.getMessage();
					if ( msg.startsWith( "cvc-pattern-valid: " ) ) {
						patternValidErrorSignalled = true;
						System.err.println( "In " + elString + ": " + msg );
					} else {
						if (!( patternValidErrorSignalled && msg.startsWith( "cvc-complex-type.2.2: " ) )) {
							throw exception;
						}
						patternValidErrorSignalled = false;
					}
				}

			};
			validator.setErrorHandler( errorHandler );
			validator.validate( new DOMSource( el ) );
		} catch ( final SAXException | IOException | ParserConfigurationException e ) {
			fail( "While validating element " + elString + ": " + e );
		}
	}
	
}

/**
 * A {@link ConnectionStreamFactory} that logs the input as files in a given directory.
 * @author jdvorak
 */
class FileLoggingConnectionStreamFactory implements OAIPMHEndpoint.ConnectionStreamFactory {

	private final String logDir;
	
	/**
	 * The factory with the given directory to place the files in.
	 * @param logDir the directory for the files
	 */
	public FileLoggingConnectionStreamFactory( final String logDir ) {
		this.logDir = logDir;
	}
	
	private static final Pattern p2 = Pattern.compile( ".*\\W(set=\\w+).*" );
	private static final Pattern p1 = Pattern.compile( ".*\\W(verb=\\w+).*" );

	@Override
	public InputStream makeInputStream( final URLConnection conn ) throws IOException {
		InputStream inputStream = conn.getInputStream();
		if ( logDir != null ) {
			final Path logDirPath = Paths.get( logDir );
			Files.createDirectories( logDirPath );
			final StringBuilder sb = new StringBuilder();
			final String url2 = conn.getURL().toExternalForm();
			final Matcher m1 = p1.matcher( url2 );
			if ( m1.matches() ) {
				sb.append( m1.group( 1 ) );
			}
			final Matcher m2 = p2.matcher( url2 );
 			if ( m2.matches() ) {
 				sb.append( "__" );
 				sb.append( m2.group( 1 ) );
 			}
			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "yyyyMMdd'T'HHmmss.SSS" );
			final String logFilename = "oai-pmh--" + dtf.format( LocalDateTime.now() ) + "--" + sb.toString() + ".xml";
			inputStream = new FileSavingInputStream( inputStream, logDirPath.resolve( logFilename ) );
		}
		return inputStream;
	}
	
}