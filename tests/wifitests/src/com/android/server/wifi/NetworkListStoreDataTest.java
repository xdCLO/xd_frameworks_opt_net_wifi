/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.wifi;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.net.wifi.WifiConfiguration;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Xml;

import com.android.internal.util.FastXmlSerializer;

import org.junit.Before;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link com.android.server.wifi.NetworksListStoreData}.
 */
@SmallTest
public class NetworkListStoreDataTest {

    private static final String TEST_SSID = "WifiConfigStoreDataSSID_";
    private static final String TEST_CONNECT_CHOICE = "XmlUtilConnectChoice";
    private static final long TEST_CONNECT_CHOICE_TIMESTAMP = 0x4566;
    private static final String SINGLE_OPEN_NETWORK_DATA_XML_STRING_FORMAT =
            "<Network>\n"
                    + "<WifiConfiguration>\n"
                    + "<string name=\"ConfigKey\">%s</string>\n"
                    + "<string name=\"SSID\">%s</string>\n"
                    + "<null name=\"BSSID\" />\n"
                    + "<null name=\"PreSharedKey\" />\n"
                    + "<null name=\"WEPKeys\" />\n"
                    + "<int name=\"WEPTxKeyIndex\" value=\"0\" />\n"
                    + "<boolean name=\"HiddenSSID\" value=\"false\" />\n"
                    + "<boolean name=\"RequirePMF\" value=\"false\" />\n"
                    + "<byte-array name=\"AllowedKeyMgmt\" num=\"1\">01</byte-array>\n"
                    + "<byte-array name=\"AllowedProtocols\" num=\"0\"></byte-array>\n"
                    + "<byte-array name=\"AllowedAuthAlgos\" num=\"0\"></byte-array>\n"
                    + "<byte-array name=\"AllowedGroupCiphers\" num=\"0\"></byte-array>\n"
                    + "<byte-array name=\"AllowedPairwiseCiphers\" num=\"0\"></byte-array>\n"
                    + "<boolean name=\"Shared\" value=\"%s\" />\n"
                    + "<int name=\"Status\" value=\"2\" />\n"
                    + "<null name=\"FQDN\" />\n"
                    + "<null name=\"ProviderFriendlyName\" />\n"
                    + "<null name=\"LinkedNetworksList\" />\n"
                    + "<null name=\"DefaultGwMacAddress\" />\n"
                    + "<boolean name=\"ValidatedInternetAccess\" value=\"false\" />\n"
                    + "<boolean name=\"NoInternetAccessExpected\" value=\"false\" />\n"
                    + "<int name=\"UserApproved\" value=\"0\" />\n"
                    + "<boolean name=\"MeteredHint\" value=\"false\" />\n"
                    + "<boolean name=\"UseExternalScores\" value=\"false\" />\n"
                    + "<int name=\"NumAssociation\" value=\"0\" />\n"
                    + "<int name=\"CreatorUid\" value=\"%d\" />\n"
                    + "<null name=\"CreatorName\" />\n"
                    + "<null name=\"CreationTime\" />\n"
                    + "<int name=\"LastUpdateUid\" value=\"-1\" />\n"
                    + "<null name=\"LastUpdateName\" />\n"
                    + "<int name=\"LastConnectUid\" value=\"0\" />\n"
                    + "</WifiConfiguration>\n"
                    + "<NetworkStatus>\n"
                    + "<string name=\"SelectionStatus\">NETWORK_SELECTION_ENABLED</string>\n"
                    + "<string name=\"DisableReason\">NETWORK_SELECTION_ENABLE</string>\n"
                    + "<null name=\"ConnectChoice\" />\n"
                    + "<long name=\"ConnectChoiceTimeStamp\" value=\"-1\" />\n"
                    + "<boolean name=\"HasEverConnected\" value=\"false\" />\n"
                    + "</NetworkStatus>\n"
                    + "<IpConfiguration>\n"
                    + "<string name=\"IpAssignment\">DHCP</string>\n"
                    + "<string name=\"ProxySettings\">NONE</string>\n"
                    + "</IpConfiguration>\n"
                    + "</Network>\n";

    private static final String SINGLE_EAP_NETWORK_DATA_XML_STRING_FORMAT =
            "<Network>\n"
                    + "<WifiConfiguration>\n"
                    + "<string name=\"ConfigKey\">%s</string>\n"
                    + "<string name=\"SSID\">%s</string>\n"
                    + "<null name=\"BSSID\" />\n"
                    + "<null name=\"PreSharedKey\" />\n"
                    + "<null name=\"WEPKeys\" />\n"
                    + "<int name=\"WEPTxKeyIndex\" value=\"0\" />\n"
                    + "<boolean name=\"HiddenSSID\" value=\"false\" />\n"
                    + "<boolean name=\"RequirePMF\" value=\"false\" />\n"
                    + "<byte-array name=\"AllowedKeyMgmt\" num=\"1\">0c</byte-array>\n"
                    + "<byte-array name=\"AllowedProtocols\" num=\"0\"></byte-array>\n"
                    + "<byte-array name=\"AllowedAuthAlgos\" num=\"0\"></byte-array>\n"
                    + "<byte-array name=\"AllowedGroupCiphers\" num=\"0\"></byte-array>\n"
                    + "<byte-array name=\"AllowedPairwiseCiphers\" num=\"0\"></byte-array>\n"
                    + "<boolean name=\"Shared\" value=\"%s\" />\n"
                    + "<int name=\"Status\" value=\"2\" />\n"
                    + "<null name=\"FQDN\" />\n"
                    + "<null name=\"ProviderFriendlyName\" />\n"
                    + "<null name=\"LinkedNetworksList\" />\n"
                    + "<null name=\"DefaultGwMacAddress\" />\n"
                    + "<boolean name=\"ValidatedInternetAccess\" value=\"false\" />\n"
                    + "<boolean name=\"NoInternetAccessExpected\" value=\"false\" />\n"
                    + "<int name=\"UserApproved\" value=\"0\" />\n"
                    + "<boolean name=\"MeteredHint\" value=\"false\" />\n"
                    + "<boolean name=\"UseExternalScores\" value=\"false\" />\n"
                    + "<int name=\"NumAssociation\" value=\"0\" />\n"
                    + "<int name=\"CreatorUid\" value=\"%d\" />\n"
                    + "<null name=\"CreatorName\" />\n"
                    + "<null name=\"CreationTime\" />\n"
                    + "<int name=\"LastUpdateUid\" value=\"-1\" />\n"
                    + "<null name=\"LastUpdateName\" />\n"
                    + "<int name=\"LastConnectUid\" value=\"0\" />\n"
                    + "</WifiConfiguration>\n"
                    + "<NetworkStatus>\n"
                    + "<string name=\"SelectionStatus\">NETWORK_SELECTION_ENABLED</string>\n"
                    + "<string name=\"DisableReason\">NETWORK_SELECTION_ENABLE</string>\n"
                    + "<null name=\"ConnectChoice\" />\n"
                    + "<long name=\"ConnectChoiceTimeStamp\" value=\"-1\" />\n"
                    + "<boolean name=\"HasEverConnected\" value=\"false\" />\n"
                    + "</NetworkStatus>\n"
                    + "<IpConfiguration>\n"
                    + "<string name=\"IpAssignment\">DHCP</string>\n"
                    + "<string name=\"ProxySettings\">NONE</string>\n"
                    + "</IpConfiguration>\n"
                    + "<WifiEnterpriseConfiguration>\n"
                    + "<string name=\"Identity\"></string>\n"
                    + "<string name=\"AnonIdentity\"></string>\n"
                    + "<string name=\"Password\"></string>\n"
                    + "<string name=\"ClientCert\"></string>\n"
                    + "<string name=\"CaCert\"></string>\n"
                    + "<string name=\"SubjectMatch\"></string>\n"
                    + "<string name=\"Engine\"></string>\n"
                    + "<string name=\"EngineId\"></string>\n"
                    + "<string name=\"PrivateKeyId\"></string>\n"
                    + "<string name=\"AltSubjectMatch\"></string>\n"
                    + "<string name=\"DomSuffixMatch\"></string>\n"
                    + "<string name=\"CaPath\"></string>\n"
                    + "<int name=\"EapMethod\" value=\"2\" />\n"
                    + "<int name=\"Phase2Method\" value=\"0\" />\n"
                    + "</WifiEnterpriseConfiguration>\n"
                    + "</Network>\n";

    private NetworkListStoreData mNetworkListStoreData;

    @Before
    public void setUp() throws Exception {
        mNetworkListStoreData = new NetworkListStoreData();
    }

    /**
     * Helper function for serializing configuration data to a XML block.
     *
     * @param shared Flag indicating serializing shared or user configurations
     * @return byte[] of the XML data
     * @throws Exception
     */
    private byte[] serializeData(boolean shared) throws Exception {
        final XmlSerializer out = new FastXmlSerializer();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        out.setOutput(outputStream, StandardCharsets.UTF_8.name());
        mNetworkListStoreData.serializeData(out, shared);
        out.flush();
        return outputStream.toByteArray();
    }

    /**
     * Helper function for parsing configuration data from a XML block.
     *
     * @param data XML data to parse from
     * @param shared Flag indicating parsing of shared or user configurations
     * @return List of WifiConfiguration parsed
     * @throws Exception
     */
    private List<WifiConfiguration> deserializeData(byte[] data, boolean shared) throws Exception {
        final XmlPullParser in = Xml.newPullParser();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        in.setInput(inputStream, StandardCharsets.UTF_8.name());
        mNetworkListStoreData.deserializeData(in, in.getDepth(), shared);
        if (shared) {
            return mNetworkListStoreData.getSharedConfigurations();
        } else {
            return mNetworkListStoreData.getUserConfigurations();
        }
    }

    /**
     * Helper function for generating a network list for testing purpose.  The network list
     * will contained an open and an EAP network.
     *
     * @param shared Flag indicating shared network
     * @return List of WifiConfiguration
     */
    private List<WifiConfiguration> getTestNetworksConfig(boolean shared) {
        WifiConfiguration openNetwork = WifiConfigurationTestUtil.createOpenNetwork();
        openNetwork.shared = shared;
        openNetwork.setIpConfiguration(
                WifiConfigurationTestUtil.createDHCPIpConfigurationWithNoProxy());
        WifiConfiguration eapNetwork = WifiConfigurationTestUtil.createEapNetwork();
        eapNetwork.shared = shared;
        eapNetwork.setIpConfiguration(
                WifiConfigurationTestUtil.createDHCPIpConfigurationWithNoProxy());
        List<WifiConfiguration> networkList = new ArrayList<>();
        networkList.add(openNetwork);
        networkList.add(eapNetwork);
        return networkList;
    }

    /**
     * Helper function for generating XML block containing two networks, an open and an EAP
     * network.
     *
     * @param openNetwork The WifiConfiguration for an open network
     * @param eapNetwork The WifiConfiguration for an EAP network
     * @return byte[] of the XML data
     */
    private byte[] getTestNetworksXmlBytes(WifiConfiguration openNetwork,
            WifiConfiguration eapNetwork) {
        String openNetworkXml = String.format(SINGLE_OPEN_NETWORK_DATA_XML_STRING_FORMAT,
                openNetwork.configKey().replaceAll("\"", "&quot;"),
                openNetwork.SSID.replaceAll("\"", "&quot;"),
                openNetwork.shared, openNetwork.creatorUid);
        String eapNetworkXml = String.format(SINGLE_EAP_NETWORK_DATA_XML_STRING_FORMAT,
                eapNetwork.configKey().replaceAll("\"", "&quot;"),
                eapNetwork.SSID.replaceAll("\"", "&quot;"),
                eapNetwork.shared, eapNetwork.creatorUid);
        return (openNetworkXml + eapNetworkXml).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Verify that serializing the store data without any configuration doesn't cause any crash
     * and no data should be serialized.
     *
     * @throws Exception
     */
    @Test
    public void serializeEmptyConfigs() throws Exception {
        assertEquals(0, serializeData(true /* shared */).length);
        assertEquals(0, serializeData(false /* shared */).length);
    }

    /**
     * Verify that parsing an empty data doesn't cause any crash and no configuration should
     * be parsed.
     *
     * @throws Exception
     */
    @Test
    public void deserializeEmptyData() throws Exception {
        assertTrue(deserializeData(new byte[0], true /* shared */).isEmpty());
        assertTrue(deserializeData(new byte[0], false /* shared */).isEmpty());
    }

    /**
     * Verify that NetworkListStoreData does support share data.
     *
     * @throws Exception
     */
    @Test
    public void supportShareData() throws Exception {
        assertTrue(mNetworkListStoreData.supportShareData());
    }

    /**
     * Verify that the shared configurations (containing an open and an EAP network) are serialized
     * correctly, matching the expected XML string.
     *
     * @throws Exception
     */
    @Test
    public void serializeSharedConfigurations() throws Exception {
        List<WifiConfiguration> networkList = getTestNetworksConfig(true /* shared */);
        mNetworkListStoreData.setSharedConfigurations(networkList);
        byte[] expectedData = getTestNetworksXmlBytes(networkList.get(0), networkList.get(1));
        assertTrue(Arrays.equals(expectedData, serializeData(true /* shared */)));
    }

    /**
     * Verify that the shared configurations are parsed correctly from a XML string containing
     * test networks (an open and an EAP network).
     * @throws Exception
     */
    @Test
    public void deserializeSharedConfigurations() throws Exception {
        List<WifiConfiguration> networkList = getTestNetworksConfig(true /* shared */);
        byte[] xmlData = getTestNetworksXmlBytes(networkList.get(0), networkList.get(1));
        WifiConfigurationTestUtil.assertConfigurationsEqualForConfigStore(
                networkList, deserializeData(xmlData, true /* shared */));
    }

    /**
     * Verify that the user configurations (containing an open and an EAP network) are serialized
     * correctly, matching the expected XML string.
     *
     * @throws Exception
     */
    @Test
    public void serializeUserConfigurations() throws Exception {
        List<WifiConfiguration> networkList = getTestNetworksConfig(false /* shared */);
        mNetworkListStoreData.setUserConfigurations(networkList);
        byte[] expectedData = getTestNetworksXmlBytes(networkList.get(0), networkList.get(1));
        assertTrue(Arrays.equals(expectedData, serializeData(false /* shared */)));
    }

    /**
     * Verify that the user configurations are parsed correctly from a XML string containing
     * test networks (an open and an EAP network).
     * @throws Exception
     */
    @Test
    public void deserializeUserConfigurations() throws Exception {
        List<WifiConfiguration> networkList = getTestNetworksConfig(false /* shared */);
        byte[] xmlData = getTestNetworksXmlBytes(networkList.get(0), networkList.get(1));
        WifiConfigurationTestUtil.assertConfigurationsEqualForConfigStore(
                networkList, deserializeData(xmlData, false /* shared */));
    }

    /**
     * Verify that a XmlPullParserException will be thrown when parsing a <Network> block
     * containing an unknown tag.
     *
     * @throws Exception
     */
    @Test(expected = XmlPullParserException.class)
    public void parseNetworkWithUnknownTag() throws Exception {
        String configFormat =
                "<Network>\n"
                        + "<WifiConfiguration>\n"
                        + "<string name=\"ConfigKey\">%s</string>\n"
                        + "<string name=\"SSID\">%s</string>\n"
                        + "<null name=\"BSSID\" />\n"
                        + "<null name=\"PreSharedKey\" />\n"
                        + "<null name=\"WEPKeys\" />\n"
                        + "<int name=\"WEPTxKeyIndex\" value=\"0\" />\n"
                        + "<boolean name=\"HiddenSSID\" value=\"false\" />\n"
                        + "<boolean name=\"RequirePMF\" value=\"false\" />\n"
                        + "<byte-array name=\"AllowedKeyMgmt\" num=\"1\">01</byte-array>\n"
                        + "<byte-array name=\"AllowedProtocols\" num=\"0\"></byte-array>\n"
                        + "<byte-array name=\"AllowedAuthAlgos\" num=\"0\"></byte-array>\n"
                        + "<byte-array name=\"AllowedGroupCiphers\" num=\"0\"></byte-array>\n"
                        + "<byte-array name=\"AllowedPairwiseCiphers\" num=\"0\"></byte-array>\n"
                        + "<boolean name=\"Shared\" value=\"%s\" />\n"
                        + "<null name=\"FQDN\" />\n"
                        + "<null name=\"ProviderFriendlyName\" />\n"
                        + "<null name=\"LinkedNetworksList\" />\n"
                        + "<null name=\"DefaultGwMacAddress\" />\n"
                        + "<boolean name=\"ValidatedInternetAccess\" value=\"false\" />\n"
                        + "<boolean name=\"NoInternetAccessExpected\" value=\"false\" />\n"
                        + "<int name=\"UserApproved\" value=\"0\" />\n"
                        + "<boolean name=\"MeteredHint\" value=\"false\" />\n"
                        + "<boolean name=\"UseExternalScores\" value=\"false\" />\n"
                        + "<int name=\"NumAssociation\" value=\"0\" />\n"
                        + "<int name=\"CreatorUid\" value=\"%d\" />\n"
                        + "<null name=\"CreatorName\" />\n"
                        + "<null name=\"CreationTime\" />\n"
                        + "<int name=\"LastUpdateUid\" value=\"-1\" />\n"
                        + "<null name=\"LastUpdateName\" />\n"
                        + "<int name=\"LastConnectUid\" value=\"0\" />\n"
                        + "</WifiConfiguration>\n"
                        + "<NetworkStatus>\n"
                        + "<string name=\"SelectionStatus\">NETWORK_SELECTION_ENABLED</string>\n"
                        + "<string name=\"DisableReason\">NETWORK_SELECTION_ENABLE</string>\n"
                        + "<null name=\"ConnectChoice\" />\n"
                        + "<long name=\"ConnectChoiceTimeStamp\" value=\"-1\" />\n"
                        + "<boolean name=\"HasEverConnected\" value=\"false\" />\n"
                        + "</NetworkStatus>\n"
                        + "<IpConfiguration>\n"
                        + "<string name=\"IpAssignment\">DHCP</string>\n"
                        + "<string name=\"ProxySettings\">NONE</string>\n"
                        + "</IpConfiguration>\n"
                        + "<Unknown>"       // Unknown tag.
                        + "<int name=\"test\" value=\"0\" />\n"
                        + "</Unknown>"
                        + "</Network>\n";
        WifiConfiguration openNetwork = WifiConfigurationTestUtil.createOpenNetwork();
        byte[] xmlData = String.format(configFormat,
                openNetwork.configKey().replaceAll("\"", "&quot;"),
                openNetwork.SSID.replaceAll("\"", "&quot;"),
                openNetwork.shared, openNetwork.creatorUid).getBytes(StandardCharsets.UTF_8);
        deserializeData(xmlData, true);
    }

    /**
     * Verify that a XmlPullParseException will be thrown when parsing a network configuration
     * containing a mismatched config key.
     *
     * @throws Exception
     */
    @Test(expected = XmlPullParserException.class)
    public void parseNetworkWithMismatchConfigKey() throws Exception {
        WifiConfiguration openNetwork = WifiConfigurationTestUtil.createOpenNetwork();
        byte[] xmlData = String.format(SINGLE_OPEN_NETWORK_DATA_XML_STRING_FORMAT,
                "InvalidConfigKey",
                openNetwork.SSID.replaceAll("\"", "&quot;"),
                openNetwork.shared, openNetwork.creatorUid).getBytes(StandardCharsets.UTF_8);
        deserializeData(xmlData, true);
    }
}