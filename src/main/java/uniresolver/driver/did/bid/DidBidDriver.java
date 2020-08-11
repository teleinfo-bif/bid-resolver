
package uniresolver.driver.did.bid;

import com.google.gson.*;
import did.DIDDocument;
import did.PublicKey;
import did.Service;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DidBidDriver implements Driver {

    private static Logger log = LoggerFactory.getLogger(DidBidDriver.class);

    public static final Pattern DID_BID_PATTERN = Pattern.compile(Constants.DID_BID_PATTERN);

    public static final String DEFAULT_BID_URL = Constants.DEFAULT_BID_URL;

    public static final Gson gson = new Gson();

    private HttpClient httpClient = HttpClients.createDefault();

    private Map<String, Object> properties = new HashMap<>();

    public DidBidDriver() {
    }

    @Override
    public ResolveResult resolve(String identifier) throws ResolutionException {

        //match identifier
        Matcher matcher = DID_BID_PATTERN.matcher(identifier);
        if (!matcher.matches()) {
            return null;
        }
        //construct resolve url
        String resolveUrl = DEFAULT_BID_URL + "/" + identifier;
        HttpGet httpGet = new HttpGet(resolveUrl);
        ResolveResult resolveResult;
        try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ResolutionException("Cannot retrieve DDO for `" + identifier + "` from `" + DEFAULT_BID_URL + ": " + httpResponse.getStatusLine());
            }

            // extract payload
            HttpEntity httpEntity = httpResponse.getEntity();
            String entityString = EntityUtils.toString(httpEntity);
            EntityUtils.consume(httpEntity);

            // check if exist identifier
            if (entityString == null) {
                throw new ResolutionException("docuement not exist");
            }
            JsonObject jsonResponse = gson.fromJson(entityString, JsonObject.class);
            JsonElement jsonDataElement = jsonResponse == null ? null : jsonResponse.get("data");

            JsonObject jsonDataObject = (jsonDataElement == null || jsonDataElement instanceof JsonNull) ? null : gson.fromJson(jsonDataElement.toString(), JsonObject.class);
            String context = "https://w3id.org/did/v1";

            if (jsonDataObject == null || jsonDataObject.get("name") == null) {
                throw new ResolutionException("docuement not exist");
            }
            String name = jsonDataObject == null ? "" : jsonDataObject.get("name").getAsString();
            String type = jsonDataObject == null ? "" : jsonDataObject.get("type").getAsString();
            String extra = jsonDataObject == null ? "" : jsonDataObject.get("extra").getAsString();
            Boolean isEnable = jsonDataObject == null ? null : jsonDataObject.get("isEnable").getAsBoolean();
            String created = jsonDataObject == null ? "" : jsonDataObject.get("created").getAsString();
            String updated = jsonDataObject == null ? "" : jsonDataObject.get("updated").getAsString();
            String balance = jsonDataObject == null ? "" : jsonDataObject.get("balance").getAsString();
            JsonElement creation = jsonDataObject == null ? null : jsonDataObject.get("creation");
            JsonElement update = jsonDataObject == null ? null : jsonDataObject.get("update");
            JsonElement currentBlock = jsonDataObject == null ? null : jsonDataObject.get("currentBlock");
            JsonElement proof = jsonDataObject == null ? null : jsonDataObject.get("proof");
            List<PublicKey> publicKeys = new ArrayList<PublicKey>();
            JsonArray publicKey = jsonDataObject == null ? null : jsonDataObject.getAsJsonArray("publicKey");
            if (publicKey != null) {
                for (JsonElement publicKeyElement : publicKey) {
                    JsonObject jsonObject = publicKeyElement == null ? null : publicKeyElement.getAsJsonObject();
                    String id = jsonObject == null ? null : jsonObject.get("id").getAsString();
                    if (id != null) {
                        String publicKeyType = jsonObject.get("type").getAsString();
                        String publicKeyString = jsonObject.get("publicKey").getAsString();
                        publicKeys.add(PublicKey.build(id, new String[]{publicKeyType}, null, publicKeyString, null, null));
                    }
                }
            }
            JsonArray auth = jsonDataObject == null ? null : jsonDataObject.getAsJsonArray("authentication");

            //build service
            List<Service> services = new ArrayList<>();
            JsonArray service = jsonDataObject == null ? null : jsonDataObject.getAsJsonArray("service");
            if (service != null) {
                for (JsonElement serviceElement : service) {
                    JsonObject jsonObject = serviceElement == null ? null : serviceElement.getAsJsonObject();
                    services.add(Service.build(gson.fromJson(jsonObject, Map.class)));
                }
            }
            //build metaData
            Map<String, Object> methodMetadata = new LinkedHashMap<String, Object>();
            methodMetadata.put("name", name);
            methodMetadata.put("type", type);
            methodMetadata.put("extra", extra);
            methodMetadata.put("isEnable", isEnable);
            methodMetadata.put("created", created);
            methodMetadata.put("updated", updated);
            methodMetadata.put("balance", balance);
            methodMetadata.put("creation", gson.fromJson(creation, Map.class));
            methodMetadata.put("update", gson.fromJson(update, Map.class));
            methodMetadata.put("currentBlock", gson.fromJson(currentBlock, Map.class));
            methodMetadata.put("proof", proof == null ? "" : gson.fromJson(proof, Map.class));
            methodMetadata.put("authentication", auth == null ? null : gson.fromJson(auth, ArrayList.class));
            DIDDocument didDocument = DIDDocument.build(context, identifier, publicKeys, null, services);

            //build resolve result
            resolveResult = ResolveResult.build(didDocument, null, DIDDocument.MIME_TYPE, null, methodMetadata);
        } catch (IOException e) {
            throw new ResolutionException("Cannot retrieve DDO info for `" + identifier + "` from `" + DEFAULT_BID_URL + "`: " + e.getMessage(), e);
        }
        return resolveResult;
    }

    @Override
    public Map<String, Object> properties() throws ResolutionException {
        return properties;
    }


    public HttpClient getHttpClient() {
        return this.httpClient;
    }
}
