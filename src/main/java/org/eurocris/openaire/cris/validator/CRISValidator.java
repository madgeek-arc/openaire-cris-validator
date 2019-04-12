package org.eurocris.openaire.cris.validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.OAIPMHEndpoint.ConnectionStreamFactory;
import org.eurocris.openaire.cris.validator.tree.CERIFNode;
import org.eurocris.openaire.cris.validator.util.CheckingIterable;
import org.eurocris.openaire.cris.validator.util.FileSavingInputStream;
import org.eurocris.openaire.cris.validator.util.XmlUtils;
import org.openarchives.oai._2.*;
import org.openarchives.oai._2_0.oai_identifier.OaiIdentifierType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * Validating a given OAI-PMH endpoint for compliance with the OpenAIRE Guidelines for CRIS Managers 1.1.
 *
 * @author jdvorak001
 * @see <a href="https://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/">the text of the specification</a>
 * @see <a href="https://github.com/openaire/guidelines-cris-managers">the github project of the specification, XML Schema and examples</a>
 * @see <a href="https://github.com/jdvorak001/openaire-cris-validator">this project on GitHub</a>
 */
public class CRISValidator {

    private static final Logger logger = LogManager.getLogger(CRISValidator.class);

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
    public static final String OPENAIRE_CERIF_XMLNS = "https://www.openaire.eu/cerif-profile/1.1/";

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
     * Method names used for the validation. (The methods are executed using reflection)
     * (*Can potentially become a user defined list of validation methods)
     */
    protected static final String[] methods = new String[]{
            "check000_Identify",
            "check010_MetadataFormats",
            "check020_Sets",
            "check100_CheckPublications",
            "check200_CheckProducts",
            "check300_CheckPatents",
            "check400_CheckPersons",
            "check500_CheckOrgUnits",
            "check600_CheckProjects",
            "check700_CheckFundings",
            "check800_CheckEquipment",
            "check900_CheckEvents",
            "check990_CheckReferentialIntegrityAndFunctionalDependency"
    };

    private static ThreadLocal<OAIPMHEndpoint> endpoint = new ThreadLocal<>();

    public static ThreadLocal<OAIPMHEndpoint> getEndpoint() {
        return endpoint;
    }

    /**
     * Invokes the validation method named {@param methodName}.
     *
     * @param methodName
     * @return
     * @throws NoSuchMethodException
     */
    public String invokeMethod(String methodName) throws NoSuchMethodException {
        Method method = CRISValidator.class.getMethod(methodName);
        String ret = "ok";
        try {
            method.invoke(this);
        } catch (Throwable e) {
            ret = e.getCause().getMessage();
        }
        return ret;
    }

    /**
     * Method that executes the validation tests, gathers their output and returns them at a {@link Map}.
     *
     * @return {@link Map}
     */
    public Map<String, String> executeTests() {
        Map<String, String> methodResults = new TreeMap<>();
        try {
            for (int i = 0; i < methods.length; i++) {
                methodResults.put(methods[i], invokeMethod(methods[i]));
            }
        } catch (NoSuchMethodException e) {
            logger.error("ERROR", e);
        }
        return methodResults;
    }

    /**
     * Set up a CRIS Validation for the URL {@param endpointUrl}.
     * The parameter {@param id} must be unique among simultaneous validations.
     *
     * @param endpointUrl
     * @param id
     * @throws MalformedURLException
     * @throws SAXException
     */
    public CRISValidator(String endpointUrl, String id) throws MalformedURLException, SAXException {
        endpoint.set(
                new OAIPMHEndpoint(new URL(endpointUrl), getParserSchema(), new FileLoggingConnectionStreamFactory("data/" + id))
        );
    }

    /**
     * @return the URL of the endpoint
     */
    public String getName() {
        return endpoint.get().getBaseUrl();
    }

    private static Schema parserSchema = null;

    /**
     * Create the schema for the validating XML parser.
     *
     * @return the compound schema
     * @throws SAXException when problem reading the schema
     */
    protected static synchronized Schema getParserSchema() throws SAXException {
        if (parserSchema == null) {
            parserSchema = getXmlSchemaFactory().newSchema(new Source[]{
                    schema("/cached/xml.xsd", "http://www.w3.org/2001/xml.xsd"),
                    schema("/cached/oai-identifier.xsd"),
                    schema("/cached/simpledc20021212.xsd", "http://dublincore.org/schemas/xmls/simpledc20021212.xsd"),
                    schema("/cached/oai_dc.xsd"),
                    schema("/cached/provenance.xsd", "http://www.openarchives.org/OAI/2.0/provenance.xsd"),
                    schema("/cached/OAI-PMH.xsd"),
                    schema("/relaxed/openaire-cerif-profile.xsd"),
            });
        }
        return parserSchema;
    }

    private static Schema validatorSchema = null;

    /**
     * Create the schema for the second-phase validation.
     *
     * @return the compound schema
     * @throws SAXException when problem reading the schema
     */
    protected static synchronized Schema getValidatorSchema() {
        if (validatorSchema == null) {
            try {
                validatorSchema = getXmlSchemaFactory().newSchema(new Source[]{
                        schema("/cached/xml.xsd", "http://www.w3.org/2001/xml.xsd"),
                        schema("/openaire-cerif-profile.xsd"),
                });
            } catch (final SAXException e) {
                throw new IllegalStateException("While initializing validator schema", e);
            }
        }
        return validatorSchema;
    }

    private static SchemaFactory xmlSchemaFactory = null;

    private static synchronized SchemaFactory getXmlSchemaFactory() {
        if (xmlSchemaFactory == null) {
            xmlSchemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        }
        return xmlSchemaFactory;
    }

    private static Source schema(final String path) {
        return schema(path, null);
    }

    private static Source schema(final String path, final String externalUrl) {
        final String path1 = "/schemas" + path;
        final URL url = OAIPMHEndpoint.class.getResource(path1);
        if (url == null) {
            throw new IllegalArgumentException("Resource " + path1 + " not found");
        }
        final StreamSource src = new StreamSource();
        src.setInputStream(OAIPMHEndpoint.class.getResourceAsStream(path1));
        src.setSystemId((externalUrl != null) ? externalUrl : url.toExternalForm());
        return src;
    }

    @SuppressWarnings("unused")
    private static Optional<String> sampleIdentifier = Optional.empty();

    private static Optional<String> serviceAcronym = Optional.empty();

    /**
     * Ask for ?verb=Identity and test it for consistence – checks (1).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check000_Identify() throws Exception {
        final IdentifyType identify = endpoint.get().callIdentify();
        CheckingIterable<DescriptionType> checker = CheckingIterable.over(identify.getDescription());
        checker = checker.checkContainsOne(new Predicate<DescriptionType>() {

            @Override
            public boolean test(final DescriptionType description) {
                final Object obj = description.getAny();
                if (obj instanceof JAXBElement<?>) {
                    final JAXBElement<?> jaxbEl = (JAXBElement<?>) obj;
                    final Object obj1 = jaxbEl.getValue();
                    if (obj1 instanceof OaiIdentifierType) {
                        final OaiIdentifierType oaiIdentifier = (OaiIdentifierType) obj1;
                        sampleIdentifier = Optional.ofNullable(oaiIdentifier.getSampleIdentifier());
                        return true;
                    }
                }
                return false;
            }

        }, "the Identify descriptions list (1b)", "an 'oai-identifier' element");
        checker = checker.checkContainsOne(new Predicate<DescriptionType>() {

            @Override
            public boolean test(final DescriptionType description) {
                final Object obj = description.getAny();
                if (obj instanceof Element) {
                    final Element el = (Element) obj;
                    if ("Service".equals(el.getLocalName()) && OPENAIRE_CERIF_XMLNS.equals(el.getNamespaceURI())) {
                        serviceAcronym = XmlUtils.getTextContents(XmlUtils.getFirstMatchingChild(el, "Acronym", el.getNamespaceURI()));
                        validateMetadataPayload(el);
                        return true;
                    }
                }
                return false;
            }

        }, "the Identify descriptions list (1a)", "a 'Service' element");
        checker.run();
        if (!endpoint.get().getBaseUrl().startsWith("file:")) {
            if (!endpoint.get().getBaseUrl().equals(identify.getBaseURL())) {
                throw new Exception("Identify response has a different endpoint base URL (1d)");
            }
        }
        final Optional<String> repoIdentifier = endpoint.get().getRepositoryIdentifer();
        if (serviceAcronym.isPresent() && repoIdentifier.isPresent()) {
            if (!serviceAcronym.get().equals(repoIdentifier.get())) {
                throw new Exception("Service acronym is not the same as the repository identifier (1c)");
            }
        }
    }

    /**
     * Ask for ?verb=ListMetadataFormats and test it for consistence – checks (2).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check010_MetadataFormats() throws Exception {
        CheckingIterable<MetadataFormatType> checker = CheckingIterable.over(endpoint.get().callListMetadataFormats().getMetadataFormat());
        checker = checker.checkUnique(MetadataFormatType::getMetadataPrefix, "Metadata prefix not unique");
        checker = checker.checkUnique(MetadataFormatType::getMetadataNamespace, "Metadata namespace not unique");
        checker = checker.checkUnique(MetadataFormatType::getSchema, "Metadata schema location not unique");
        checker = wrapCheckMetadataFormatPresent(checker, OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CERIF_XMLNS);
        checker.run();
    }

    private CheckingIterable<MetadataFormatType> wrapCheckMetadataFormatPresent(final CheckingIterable<MetadataFormatType> parent, final String expectedMetadataFormatPrefix, final String expectedMetadataFormatNamespace) {
        final Predicate<MetadataFormatType> predicate = new Predicate<MetadataFormatType>() {

            @Override
            public boolean test(final MetadataFormatType mf) {
                if (expectedMetadataFormatPrefix.equals(mf.getMetadataPrefix())) {
                    if (!expectedMetadataFormatNamespace.equals(mf.getMetadataNamespace())) {
                        throw new RuntimeException(String.format("Non-matching set name for set '%s' (2)", expectedMetadataFormatPrefix));
                    }
                    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    dbf.setValidating(false);
                    dbf.setIgnoringComments(true);
                    try {
                        final DocumentBuilder db = dbf.newDocumentBuilder();
                        final String schemaUrl = mf.getSchema();
                        if (!schemaUrl.startsWith(OPENAIRE_CERIF_SCHEMAS_ROOT)) {
                            logger.error(String.format("Schema url: %s - should start with: %s", schemaUrl, OPENAIRE_CERIF_SCHEMAS_ROOT));
                            throw new RuntimeException("Please reference the official XML Schema at " + OPENAIRE_CERIF_SCHEMAS_ROOT + " (2)");
                        }
                        if (!schemaUrl.endsWith("/" + OPENAIRE_CERIF_SCHEMA_FILENAME)) {
                            logger.error(String.format("Schema url: %s - should end with: %s", schemaUrl, OPENAIRE_CERIF_SCHEMA_FILENAME));
                            throw new RuntimeException("The schema file should be " + OPENAIRE_CERIF_SCHEMA_FILENAME + " (2)");
                        }
                        final String realSchemaUrl = (schemaUrl.equals(CURRENT_XML_SCHEMA_URL_PREFIX + OPENAIRE_CERIF_SCHEMA_FILENAME))
                                ? this.getClass().getResource("/schemas/openaire-cerif-profile.xsd").toExternalForm()
                                : schemaUrl;
                        logger.info("Fetching " + realSchemaUrl);
                        final Document doc = db.parse(realSchemaUrl);
                        final Element schemaRootEl = doc.getDocumentElement();
                        final String targetNsUri = schemaRootEl.getAttribute("targetNamespace");
                        if (!mf.getMetadataNamespace().equals(targetNsUri)) {
                            logger.error("The schema does not have the advertised target namespace URI (2)");
                            throw new RuntimeException("The schema does not have the advertised target namespace URI (2)");
                        }
                    } catch (final ParserConfigurationException | SAXException | IOException e) {
                        throw new IllegalStateException(e);
                    }
                    return true;
                }
                return false;
            }

        };
        return parent.checkContains(predicate, new AssertionError("MetadataFormat '" + expectedMetadataFormatPrefix + "' not present (2)"));
    }

    /**
     * Ask for ?verb=ListSets and test it for consistence – checks (3).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check020_Sets() throws Exception {
        CheckingIterable<SetType> checker = CheckingIterable.over(endpoint.get().callListSets());
        checker = checker.checkUnique(SetType::getSetSpec, "setSpec not unique");
        checker = wrapCheckSetPresent(checker, OPENAIRE_CRIS_PUBLICATIONS__SET_SPEC, "OpenAIRE_CRIS_publications");
        checker = wrapCheckSetPresent(checker, OPENAIRE_CRIS_PRODUCTS__SET_SPEC, "OpenAIRE_CRIS_products");
        checker = wrapCheckSetPresent(checker, OPENAIRE_CRIS_PATENTS__SET_SPEC, "OpenAIRE_CRIS_patents");
        checker = wrapCheckSetPresent(checker, OPENAIRE_CRIS_PERSONS__SET_SPEC, "OpenAIRE_CRIS_persons");
        checker = wrapCheckSetPresent(checker, OPENAIRE_CRIS_ORGUNITS__SET_SPEC, "OpenAIRE_CRIS_orgunits");
        checker = wrapCheckSetPresent(checker, OPENAIRE_CRIS_PROJECTS__SET_SPEC, "OpenAIRE_CRIS_projects");
        checker = wrapCheckSetPresent(checker, OPENAIRE_CRIS_FUNDING__SET_SPEC, "OpenAIRE_CRIS_funding");
        checker = wrapCheckSetPresent(checker, OPENAIRE_CRIS_EVENTS__SET_SPEC, "OpenAIRE_CRIS_events");
        checker = wrapCheckSetPresent(checker, OPENAIRE_CRIS_EQUIPMENTS__SET_SPEC, "OpenAIRE_CRIS_equipments");
        checker.run();
    }

    private CheckingIterable<SetType> wrapCheckSetPresent(final CheckingIterable<SetType> parent, final String expectedSetSpec, final String expectedSetName) {
        final Predicate<SetType> predicate = new Predicate<SetType>() {

            @Override
            public boolean test(final SetType s) {
                if (expectedSetSpec.equals(s.getSetSpec())) {
                    if (!expectedSetName.equals(s.getSetName())) {
                        logger.error(String.format("Non-matching set name for set '%s' (3)", expectedSetSpec));
                        throw new RuntimeException(String.format("Non-matching set name for set '%s' (3)", expectedSetSpec));
                    }
                    return true;
                }
                return false;
            }

        };
        return parent.checkContains(predicate, new AssertionError("Set '" + expectedSetSpec + "' not present (3)"));
    }

    /**
     * Ask for ?verb=ListRecords on the products set and test it for consistence – checks (5).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check100_CheckPublications() throws Exception {
        final Iterable<RecordType> records = endpoint.get().callListRecords(OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PUBLICATIONS__SET_SPEC, null, null);
        final CheckingIterable<RecordType> checker = buildCommonCheckersChain(records, "Publication");
        checker.run();
    }

    /**
     * Ask for ?verb=ListRecords on the publications set and test it for consistence – checks (5).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check200_CheckProducts() throws Exception {
        final Iterable<RecordType> records = endpoint.get().callListRecords(OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PRODUCTS__SET_SPEC, null, null);
        final CheckingIterable<RecordType> checker = buildCommonCheckersChain(records, "Product");
        checker.run();
    }

    /**
     * Ask for ?verb=ListRecords on the patents set and test it for consistence – checks (5).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check300_CheckPatents() throws Exception {
        final Iterable<RecordType> records = endpoint.get().callListRecords(OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PATENTS__SET_SPEC, null, null);
        final CheckingIterable<RecordType> checker = buildCommonCheckersChain(records, "Patent");
        checker.run();
    }

    /**
     * Ask for ?verb=ListRecords on the persons set and test it for consistence – checks (5).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check400_CheckPersons() throws Exception {
        final Iterable<RecordType> records = endpoint.get().callListRecords(OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PERSONS__SET_SPEC, null, null);
        final CheckingIterable<RecordType> checker = buildCommonCheckersChain(records, "Person");
        checker.run();
    }

    /**
     * Ask for ?verb=ListRecords on the organisation units set and test it for consistence – checks (5).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check500_CheckOrgUnits() throws Exception {
        final Iterable<RecordType> records = endpoint.get().callListRecords(OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_ORGUNITS__SET_SPEC, null, null);
        final CheckingIterable<RecordType> checker = buildCommonCheckersChain(records, "OrgUnit");
        checker.run();
    }

    /**
     * Ask for ?verb=ListRecords on the projects set and test it for consistence – checks (5).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check600_CheckProjects() throws Exception {
        final Iterable<RecordType> records = endpoint.get().callListRecords(OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PROJECTS__SET_SPEC, null, null);
        final CheckingIterable<RecordType> checker = buildCommonCheckersChain(records, "Project");
        checker.run();
    }

    /**
     * Ask for ?verb=ListRecords on the fundings set and test it for consistence – checks (5).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check700_CheckFundings() throws Exception {
        final Iterable<RecordType> records = endpoint.get().callListRecords(OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_FUNDING__SET_SPEC, null, null);
        final CheckingIterable<RecordType> checker = buildCommonCheckersChain(records, "Funding");
        checker.run();
    }

    /**
     * Ask for ?verb=ListRecords on the equipment set and test it for consistence – checks (5).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check800_CheckEquipment() throws Exception {
        final Iterable<RecordType> records = endpoint.get().callListRecords(OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_EQUIPMENTS__SET_SPEC, null, null);
        final CheckingIterable<RecordType> checker = buildCommonCheckersChain(records, "Equipment");
        checker.run();
    }

    /**
     * Ask for ?verb=ListRecords on the events set and test it for consistence – checks (5).
     *
     * @throws Exception on any unexpected circumstance
     */
    public void check900_CheckEvents() throws Exception {
        final Iterable<RecordType> records = endpoint.get().callListRecords(OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_EVENTS__SET_SPEC, null, null);
        final CheckingIterable<RecordType> checker = buildCommonCheckersChain(records, "Event");
        checker.run();
    }

    /**
     * Prepare the checks to run on all CERIF records.
     *
     * @param records                  the iterable containing the records
     * @param expectedElementLocalName the expected local name of the metadata elements
     * @return a {@link CheckingIterable} that checks the namespace, the uniqueness of the OAI identifiers of the records and their consistence with the types and IDs of the objects
     */
    protected CheckingIterable<RecordType> buildCommonCheckersChain(final Iterable<RecordType> records, final String expectedElementLocalName) {
        final QName expectedQName = new QName(OPENAIRE_CERIF_XMLNS, expectedElementLocalName);
        return wrapCheckPayloadQNameAndAccummulate(expectedQName,
                wrapCheckUniqueness(
                        wrapCheckOAIIdentifier(
                                CheckingIterable.over(records)
                        )
                )
        );
    }

    private CheckingIterable<RecordType> wrapCheckUniqueness(final CheckingIterable<RecordType> checker) {
        final Function<RecordType, HeaderType> f1 = RecordType::getHeader;
        return checker.checkUnique(f1.andThen(HeaderType::getIdentifier), "record identifier not unique");
    }

    private CheckingIterable<RecordType> wrapCheckOAIIdentifier(final CheckingIterable<RecordType> checker) {
        final Optional<String> repoIdentifier = endpoint.get().getRepositoryIdentifer();
        if (repoIdentifier.isPresent()) {
            final Function<RecordType, String> expectedFunction = new Function<RecordType, String>() {

                @Override
                public String apply(final RecordType x) {
                    final MetadataType metadata = x.getMetadata();
                    if (metadata != null) {
                        final Element el = (Element) metadata.getAny();
                        return "oai:" + repoIdentifier.get() + ":" + el.getLocalName() + "s/" + el.getAttribute("id");
                    } else {
                        // make the test trivially satisfied for records with no metadata
                        return x.getHeader().getIdentifier();
                    }
                }

            };
            return checker.checkForAllEquals(expectedFunction, (final RecordType record) -> (record.getHeader().getIdentifier()), "OAI identifier other than expected");
        } else {
            return checker;
        }
    }

    private static Map<String, CERIFNode> recordsByName = new HashMap<>();
    private static Map<String, CERIFNode> recordsByOaiIdentifier = new HashMap<>();

    private CheckingIterable<RecordType> wrapCheckPayloadQNameAndAccummulate(final QName expectedQName, final CheckingIterable<RecordType> checker) {
        return checker.checkForAll(new Predicate<RecordType>() {

            @Override
            public boolean test(final RecordType t) {
                final MetadataType recordMetadata = t.getMetadata();
                if (recordMetadata != null) {
                    final Object obj = recordMetadata.getAny();
                    if (obj instanceof Element) {
                        final Element el = (Element) obj;
                        if (!expectedQName.getNamespaceURI().equals(el.getNamespaceURI())) {
                            logger.error("The payload element not in the right namespace");
                            throw new RuntimeException("The payload element not in the right namespace");
                        }
                        if (!expectedQName.getLocalPart().equals(el.getLocalName())) {
                            logger.error("The payload element does not have the right local name");
                            throw new RuntimeException("The payload element does not have the right local name");
                        }
                        validateMetadataPayload(el);
                        final CERIFNode node = CERIFNode.buildTree(el);
                        recordsByName.put(node.getName(), node);
                        recordsByOaiIdentifier.put(t.getHeader().getIdentifier(), node);
                        return true;
                    }
                }
                // fail unless the record is deleted
                return StatusType.DELETED.equals(t.getHeader().getStatus());
            }

        }, "Metadata missing from OAI-PMH record");
    }

    private static final String[] types = new String[]{"Publication", "Product", "Patent", "Person", "OrgUnit", "Project", "Funding", "Event", "Equipment"};

    static {
        Arrays.sort(types);
    }

    /**
     * Test the accummulated data for consistence – checks (5a) and (5b).
     */
    public void check990_CheckReferentialIntegrityAndFunctionalDependency() {
        for (final Map.Entry<String, CERIFNode> entry : recordsByOaiIdentifier.entrySet()) {
            final String oaiIdentifier = entry.getKey();
            final CERIFNode node = entry.getValue();
            // for all harvested CERIF data, check the children of the MainForTesting objects (no need to check the objects themselves, they satisfy all checks trivially)
            for (final CERIFNode node3 : node.getChildren(null)) {
                lookForCERIFObjectsAndCheckReferentialIntegrityAndFunctionalDependency(node3, oaiIdentifier);
            }
        }
    }

    private void lookForCERIFObjectsAndCheckReferentialIntegrityAndFunctionalDependency(final CERIFNode node, final String oaiIdentifier) {
        // do the checks if this is a CERIF object
        final String type = node.getType();
        if (Arrays.binarySearch(types, type) >= 0) {
            doCheckFunctionalDependency(node, oaiIdentifier);
        }
        // recurse to children of this node
        for (final CERIFNode node2 : node.getChildren(null)) {
            lookForCERIFObjectsAndCheckReferentialIntegrityAndFunctionalDependency(node2, oaiIdentifier);
        }
    }

    private void doCheckFunctionalDependency(final CERIFNode node, final String oaiIdentifier) {
        final String name = node.getName();
        if (name.contains("[@id=\"")) {
            final CERIFNode baseNode = recordsByName.get(name);
            if (baseNode == null) {
                logger.error(String.format("Record for %s not found, referential integrity violated in %s (5a)", name, oaiIdentifier));
                throw new RuntimeException(String.format("Record for %s not found, referential integrity violated in %s (5a)", name, oaiIdentifier));
            }
            if (!node.isSubsetOf(baseNode)) {
                final CERIFNode missingNode = node.reportWhatIMiss(baseNode).get();
                throw new RuntimeException("Violation of (5b) in " + oaiIdentifier + ":\n" + node + "is not subset of\n" + baseNode + "missing is\n" + missingNode);
            }
        }
    }

    /**
     * Validate the metadata payload subtree against the XML Schema resulting from {@link #getValidatorSchema()}.
     *
     * @param el the metadata payload top element
     */
    protected void validateMetadataPayload(final Element el) {
        final String elString = el.getLocalName() + "[@id=\"" + el.getAttribute("id") + "\"]";
        final Validator validator = getValidatorSchema().newValidator();
        try {
            final ErrorHandler errorHandler = new ErrorHandler() {

                private boolean patternValidErrorSignalled = false;

                @Override
                public void warning(final SAXParseException exception) throws SAXException {
                    // do nothing
                }

                @Override
                public void fatalError(final SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void error(final SAXParseException exception) throws SAXException {
                    final String msg = exception.getMessage();
                    if (msg.startsWith("cvc-pattern-valid: ")) {
                        patternValidErrorSignalled = true;
                        logger.error("In " + elString + ": " + msg);
                    } else {
                        if (!(patternValidErrorSignalled && msg.startsWith("cvc-complex-type.2.2: "))) {
                            throw exception;
                        }
                        patternValidErrorSignalled = false;
                    }
                }

            };
            validator.setErrorHandler(errorHandler);
            validator.validate(new DOMSource(el));
        } catch (final SAXException | IOException e) {
            throw new RuntimeException("While validating element " + elString + ": ", e);
        }
    }

}

/**
 * A {@link ConnectionStreamFactory} that logs the input as files in a given directory.
 *
 * @author jdvorak
 */
class FileLoggingConnectionStreamFactory implements OAIPMHEndpoint.ConnectionStreamFactory {

    private final String logDir;

    /**
     * The factory with the given directory to place the files in.
     *
     * @param logDir the directory for the files
     */
    public FileLoggingConnectionStreamFactory(final String logDir) {
        this.logDir = logDir;
    }

    private static final Pattern p2 = Pattern.compile(".*\\W(set=\\w+).*");
    private static final Pattern p1 = Pattern.compile(".*\\W(verb=\\w+).*");

    @Override
    public InputStream makeInputStream(final URLConnection conn) throws IOException {
        InputStream inputStream = conn.getInputStream();
        if (logDir != null) {
            final Path logDirPath = Paths.get(logDir);
            Files.createDirectories(logDirPath);
            final StringBuilder sb = new StringBuilder();
            final String url2 = conn.getURL().toExternalForm();
            final Matcher m1 = p1.matcher(url2);
            if (m1.matches()) {
                sb.append(m1.group(1));
            }
            final Matcher m2 = p2.matcher(url2);
            if (m2.matches()) {
                sb.append("__");
                sb.append(m2.group(1));
            }
            final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS");
            final String logFilename = "oai-pmh--" + dtf.format(LocalDateTime.now()) + "--" + sb.toString() + ".xml";
            inputStream = new FileSavingInputStream(inputStream, logDirPath.resolve(logFilename));
        }
        return inputStream;
    }

}