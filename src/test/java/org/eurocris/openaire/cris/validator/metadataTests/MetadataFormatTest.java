package org.eurocris.openaire.cris.validator.metadataTests;

import org.eurocris.openaire.cris.validator.model.RuleResults;
import org.junit.Test;
import org.junit.runner.OrderWith;
import org.junit.runner.manipulation.Alphanumeric;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * Test the behaviour of the CRISValidator in diverse edge cases.
 * @author jdvorak001
 */
@OrderWith(Alphanumeric.class)
public class MetadataFormatTest {

	/**
	 * Test that {@link CRISValidator} reports when no OpenAIRE CRIS metadata format is offered.
	 * @see (2a) in CHECKS.md
	 * @throws Exception in case of some error
	 */
	@Test
	public void testCheck2a() throws Exception {
		final URL url = MetadataFormatTest.class.getResource( "check_2a/" );
		final CRISValidator validator = new CRISValidator( url );
		validator.check000_Identify();
		try {
			RuleResults results = validator.check010_MetadataFormats();
			assertTrue( results.hasErrorMessage("Metadata format for the OpenAIRE Guidelines for CRIS Managers not present (2a)") );
		} catch ( final AssertionError e ) {
			fail( "Problem: check (2a) undetected" );
		}
	}

	/**
	 * Test that {@link CRISValidator} reports when an OpenAIRE CRIS metadata format has a wrong namespace URI.
	 * @see (2b) in CHECKS.md
	 * @throws Exception in case of some error
	 */
	@Test
	public void testCheck2b() throws Exception {
		final URL url = MetadataFormatTest.class.getResource( "check_2b/" );
		final CRISValidator validator = new CRISValidator( url );
		validator.check000_Identify();
		try {
			RuleResults results = validator.check010_MetadataFormats();
			assertTrue( results.hasErrorMessage("The metadata NS for prefix oai_cerif_openaire does not start with " + CRISValidator.OPENAIRE_CERIF_XMLNS_PREFIX + " (2b)") );
		} catch ( final AssertionError e ) {
			fail( "Problem: check (2b) not handled exception" );
		}
	}

	/**
	 * Test that {@link CRISValidator} reports when an OpenAIRE CRIS XML namespace has a wrong metadata format.
	 * @see (2c) in CHECKS.md
	 * @throws Exception in case of some error
	 */
	@Test
	public void testCheck2c() throws Exception {
		final URL url = MetadataFormatTest.class.getResource( "check_2c/" );
		final CRISValidator validator = new CRISValidator( url );
		validator.check000_Identify();
		try {
			RuleResults results = validator.check010_MetadataFormats();
			assertTrue( results.hasErrorMessage("The metadata prefix for XML namespace https://www.openaire.eu/cerif-profile/1.2/ does not start with " + CRISValidator.OAI_CERIF_OPENAIRE__METADATA_PREFIX + " (2c)") );
		} catch ( final AssertionError e ) {
			fail( "Problem: check (2c) undetected" );
		}
	}

	/**
	 * Test that {@link CRISValidator} reports a non-unique metadata format.
	 * @see (2d) in CHECKS.md
	 * @throws Exception in case of some error
	 */
	@Test
	public void testCheck2d() throws Exception {
		final URL url = MetadataFormatTest.class.getResource( "check_2d/" );
		final CRISValidator validator = new CRISValidator( url );
		validator.check000_Identify();
		try {
			RuleResults results = validator.check010_MetadataFormats();
			assertTrue( results.hasErrorMessage("Metadata prefix not unique (2d); value: oai_cerif_openaire") );
		} catch ( final AssertionError e ) {
			fail( "Problem: check (2d) undetected" );
		}
	}

	/**
	 * Test that {@link CRISValidator} reports a non-unique metadata namespace URI.
	 * @see (2e) in CHECKS.md
	 * @throws Exception in case of some error
	 */
	@Test
	public void testCheck2e() throws Exception {
		final URL url = MetadataFormatTest.class.getResource( "check_2e/" );
		final CRISValidator validator = new CRISValidator( url );
		validator.check000_Identify();
		try {
			RuleResults results = validator.check010_MetadataFormats();
			assertTrue( results.hasErrorMessage("Metadata namespace not unique (2e); value: https://www.openaire.eu/cerif-profile/1.2/") );
		} catch ( final AssertionError e ) {
			fail( "Problem: check (2e) undetected" );
		}
	}

	/**
	 * Test that {@link CRISValidator} reports a non-unique XML Schema URL.
	 * @see (2f) in CHECKS.md
	 * @throws Exception in case of some error
	 */
	@Test
	public void testCheck2f() throws Exception {
		final URL url = MetadataFormatTest.class.getResource( "check_2f/" );
		final CRISValidator validator = new CRISValidator( url );
		validator.check000_Identify();
		try {
			RuleResults results = validator.check010_MetadataFormats();
			assertTrue( results.hasErrorMessage("Metadata schema location not unique (2f); value: https://www.openaire.eu/schema/cris/1.2/openaire-cerif-profile.xsd") );
		} catch ( final AssertionError e ) {
			fail( "Problem: check (2f) undetected" );
		}
	}

	/**
	 * Test that {@link CRISValidator} reports an unsupported OpenAIRE CRIS XML namespace.
	 * @see (2g) in CHECKS.md
	 * @throws Exception in case of some error
	 */
	@Test
	public void testCheck2g() throws Exception {
		final URL url = MetadataFormatTest.class.getResource( "check_2g/" );
		final CRISValidator validator = new CRISValidator( url );
		validator.check000_Identify();
		try {
			RuleResults results = validator.check010_MetadataFormats();
			assertTrue( results.hasErrorMessage("This validator does not cover the metadata namespace https://www.openaire.eu/cerif-profile/0.0/ (2g)") );
		} catch ( final AssertionError e ) {
			fail( "Problem: check (2g) undetected" );
		}
	}

	/**
	 * Test that {@link CRISValidator} reports an unofficial XML Schema location.
	 * @see (2h) in CHECKS.md
	 * @throws Exception in case of some error
	 */
	@Test
	public void testCheck2h() throws Exception {
		final URL url = MetadataFormatTest.class.getResource( "check_2h/" );
		final CRISValidator validator = new CRISValidator( url );
		validator.check000_Identify();
		try {
			RuleResults results = validator.check010_MetadataFormats();
			assertTrue( results.hasErrorMessage("Please reference the official XML Schema at https://www.openaire.eu/schema/cris/ (2h)") );
		} catch ( final AssertionError e ) {
			fail( "Problem: check (2h) undetected" );
		}
	}

	/**
	 * Test that {@link CRISValidator} reports an unsupported XML Schema filename.
	 * @see (2i) in CHECKS.md
	 * @throws Exception in case of some error
	 */
	@Test
	public void testCheck2i() throws Exception {
		final URL url = MetadataFormatTest.class.getResource( "check_2i/" );
		final CRISValidator validator = new CRISValidator( url );
		validator.check000_Identify();
		try {
			RuleResults results = validator.check010_MetadataFormats();
			assertTrue( results.hasErrorMessage("The schema file should be openaire-cerif-profile.xsd (2i)") );
		} catch ( final AssertionError e ) {
			fail( "Problem: check (2i) undetected" );
		}
	}

	/**
	 * Test that {@link CRISValidator} reports a declared, but unsupported 1.1 compatibility.
	 * @see (2k) in CHECKS.md
	 * @throws Exception in case of some error
	 */
	@Test
	public void testCheck2k1() throws Exception {
		final URL url = MetadataFormatTest.class.getResource( "check_2k1/" );
		final CRISValidator validator = new CRISValidator( url );
		validator.check000_Identify();
		try {
			RuleResults results = validator.check010_MetadataFormats();
			assertTrue( results.hasErrorMessage("No metadata format specified for declared compatibility https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Service_Compatibility#1.1 (2k)") );
		} catch ( final AssertionError e ) {
			fail( "Problem: check (2k) undetected" );
		}
	}

	/**
	 * Test that {@link CRISValidator} reports a declared, but unsupported 1.2 compatibility.
	 * @see (2k) in CHECKS.md
	 * @throws Exception in case of some error
	 */
	@Test
	public void testCheck2k2() throws Exception {
		final URL url = MetadataFormatTest.class.getResource( "check_2k2/" );
		final CRISValidator validator = new CRISValidator( url );
		validator.check000_Identify();
		try {
			RuleResults results = validator.check010_MetadataFormats();
			assertTrue( results.hasErrorMessage("No metadata format specified for declared compatibility https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Service_Compatibility#1.2 (2k)") );
		} catch ( final AssertionError e ) {
			fail( "Problem: check (2l) undetected" );
		}
	}

}
