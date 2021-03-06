package architecture.community.model.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import architecture.community.page.PageState;
import architecture.community.user.User.Status;

public class JsonUserStatusDeserializer  extends JsonDeserializer<Status> {
 
	@Override
	public Status deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		return Status.valueOf(p.getText().toUpperCase());
	}

}
