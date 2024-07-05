package csi.server.business.visualization.graph.pattern.neo4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class PropertyValue {
   public PropertyValue() {
   }

   public static List<String> getPropertyKeys() {
      String nodeEntryPointUri = Neo4jHelper.getRootUri() + "propertykeys";
      WebResource resource = Client.create().resource(nodeEntryPointUri);
      ClientResponse response = resource.accept("application/json", "charset=UTF-8").get(ClientResponse.class);
      List<String> keys = new ArrayList<String>();

      if (response.getStatus() == 200) {
         String responseBody = response.getEntity(String.class);
         JsonElement jsonElement = (new JsonParser()).parse(responseBody);

         if (jsonElement instanceof JsonArray) {
            JsonArray jsonArray = (JsonArray) jsonElement;
            Iterator<JsonElement> i$ = jsonArray.iterator();

            while (i$.hasNext()) {
               JsonElement element = i$.next();

               if (element instanceof JsonPrimitive) {
                  keys.add(element.getAsString());
               }
            }
         }
      }
      response.close();
      return keys;
   }
}
