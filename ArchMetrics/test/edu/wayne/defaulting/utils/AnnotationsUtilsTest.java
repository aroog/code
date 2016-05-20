package edu.wayne.defaulting.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.wayne.metrics.utils.AnnotationsUtils;

public class AnnotationsUtilsTest {

	@Test
	public final void testExtractNoItems() {
		assertEquals(1, AnnotationsUtils.extractNoItems("@Domains(\"owned\")"));
	}
	
	@Test
	public final void testExtractNoItemsPublicDomains() {
		assertEquals(2, AnnotationsUtils.extractNoItems("@Domains({\"DOMKEY\", \"DOMVALUE\"})"));
	}

	
	@Test
	public final void testExtractNoItemsDomainInherits1() {
		assertEquals(1, AnnotationsUtils.extractNoItems("@DomainInherits( { \"PEBrowser<U,L,D>\" })"));
	}
	
	@Test
	public final void testExtractNoItemsDomainParams3() {
		assertEquals(3, AnnotationsUtils.extractNoItems("@DomainParams( { \"U\", \"L\", \"D\" })"));
	}
	
	@Test
	public final void testExtractNoItemsDomainInherits2() {
		assertEquals(2, AnnotationsUtils.extractNoItems("@DomainInherits( { \"LocalizedFtpReply<U,L,D>\", \"DataTransferFtpReply<U,L,D>\"})"));
	}
	
	@Test
	public final void testExtractNoItemsDomainsPrivatePublic() {
		assertEquals(2, AnnotationsUtils.extractNoItems("@Domains( { \"owned\", \"USERS\" })"));
	}

	//private domains
	
	
	@Test
	public final void testExtractPrivateDomains2() {
		assertEquals(1, AnnotationsUtils.extractNoPrivateDomains("@Domains( { \"owned\", \"USERS\" })"));
	}
	
	@Test
	public final void testExtractNoPrivateDomains1() {
		assertEquals(1, AnnotationsUtils.extractNoPrivateDomains("@Domains(\"owned\")"));
	}
	
	@Test
	public final void testExtractNoPrivateDomains0() {
		assertEquals(0, AnnotationsUtils.extractNoPrivateDomains("@Domains(\"VIEW\")"));
	}
	
	@Test
	public final void testExtractNoPrivateDomains00() {
		assertEquals(0, AnnotationsUtils.extractNoPrivateDomains(""));
	}
	
	@Test
	public final void testExtractPublicDomains2() {
		assertEquals(1, AnnotationsUtils.extractNoPublicDomains("@Domains( { \"owned\", \"USERS\" })"));
	}
	
	@Test
	public final void testExtractNoPublicDomains1() {
		assertEquals(0, AnnotationsUtils.extractNoPublicDomains("@Domains(\"owned\")"));
	}
	
	@Test
	public final void testExtractNoPublicDomains0() {
		assertEquals(1, AnnotationsUtils.extractNoPublicDomains("@Domains(\"VIEW\")"));
	}
	
	@Test
	public final void testExtractNoPublicDomains00() {
		assertEquals(0, AnnotationsUtils.extractNoPublicDomains(""));
	}
}
