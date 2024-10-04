package com.hzn.awsopensearch.util.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzn.awsopensearch.util.ObjectUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@SuppressWarnings ({"unchecked"})
public class HttpClient {
	private final String url;
	private final Parameters parameters;
	private final Headers headers;
	private final String requestBody;
	private HttpResponse<String> httpResponse;

	private HttpClient (String url, Parameters parameters, Headers headers, String requestBody) {
		this.url = url;
		this.parameters = parameters;
		this.headers = headers;
		this.requestBody = requestBody;
	}

	public static HttpClientBuilder builder () {
		return new HttpClientBuilder ();
	}

	private HttpClient get () {
		httpResponse = request (url, HttpMethod.GET.name (), headers, parameters, null);
		return this;
	}

	private HttpClient post () {
		httpResponse = request (url, HttpMethod.POST.name (), headers, parameters, requestBody);
		return this;
	}

	private HttpClient put () {
		httpResponse = request (url, HttpMethod.PUT.name (), headers, parameters, requestBody);
		return this;
	}

	private HttpClient patch () {
		httpResponse = request (url, HttpMethod.PATCH.name (), headers, parameters, requestBody);
		return this;
	}

	private HttpClient delete () {
		httpResponse = request (url, HttpMethod.DELETE.name (), headers, parameters, null);
		return this;
	}

	public HttpResponse<Map<String, Object>> getHttpResponseByMap () {
		try {
			Map<String, Object> responseBodyMap = new ObjectMapper ().readValue (httpResponse.getData (), Map.class);
			return HttpResponse.of (httpResponse.getCode (), httpResponse.getMessage (), responseBodyMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException ("Failed to parse response body", e);
		}
	}

	public <T> HttpResponse<T> getHttpResponseByClass (Class<T> clazz) {
		try {
			T cls = new ObjectMapper ().readValue (httpResponse.getData (), clazz);
			return HttpResponse.of (httpResponse.getCode (), httpResponse.getMessage (), cls);
		} catch (JsonProcessingException e) {
			throw new RuntimeException ("Failed to parse response body", e);
		}
	}

	private HttpResponse<String> request (String spec, String method, Headers headers, Parameters parameters, String requestBody) {
		StringBuilder urlSb = new StringBuilder (spec);
		if (HttpMethod.GET.name ().equalsIgnoreCase (method) && !ObjectUtil.isEmpty (parameters)) {
			queryString (urlSb, parameters, method);
		}
		URL url;
		try {
			url = new URI (urlSb.toString ()).toURL ();
		} catch (MalformedURLException | URISyntaxException e) {
			throw new RuntimeException ("Failed to build url", e);
		}
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) url.openConnection ();
			connection.setRequestMethod (method);
		} catch (IOException e) {
			throw new RuntimeException ("Failed to open connection", e);
		}

		AtomicReference<String> contentType = new AtomicReference<> ();
		if (headers != null) {
			headers.forEach ((k, v) -> {
				String values = String.join (",", v);
				if (k.equals (Headers.CONTENT_TYPE)) {
					contentType.set (values);
				}
				connection.setRequestProperty (k, values);
			});
		}

		if (ObjectUtil.isEmpty (contentType.get ())) {
			contentType.set (MediaType.URL_ENCODED);
			connection.setRequestProperty ("Content-Type", MediaType.JSON);
		}

		if (HttpMethod.POST.name ().equalsIgnoreCase (method) || HttpMethod.PUT.name ().equalsIgnoreCase (method) || HttpMethod.PATCH.name ().equalsIgnoreCase (method)) {
			connection.setDoOutput (true);
			try (OutputStream os = connection.getOutputStream ()) {
				if (contentType.get ().contains (MediaType.JSON)) {
					if (requestBody != null) {
						os.write (requestBody.getBytes (StandardCharsets.UTF_8));
					} else {
						os.write (new ObjectMapper ().writeValueAsString (parameters).getBytes (StandardCharsets.UTF_8));
					}
				} else {
					StringBuilder sb = new StringBuilder ();
					queryString (sb, parameters, method);
					os.write (sb.toString ().getBytes (StandardCharsets.UTF_8));
				}
			} catch (IOException e) {
				throw new RuntimeException ("Failed to write parameters", e);
			}
		}

		StringBuilder sb = new StringBuilder ();
		int code;
		try {
			code = connection.getResponseCode ();
			try (BufferedReader bis = new BufferedReader (new InputStreamReader (code == 200 ? connection.getInputStream () : connection.getErrorStream ()))) {
				String line;
				while ((line = bis.readLine ()) != null) {
					sb.append (line);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException ("Failed to read response", e);
		}

		return HttpResponse.of (code, HttpStatus.valueOf (code).getMessage (), sb.toString ());
	}

	private static void queryString (StringBuilder sb, Map<String, Object> parameters, String method) {
		if (HttpMethod.GET.name ().equals (method)) {
			sb.append ("?");
		}
		int count = 0;
		for (Map.Entry<String, Object> entry : parameters.entrySet ()) {
			sb.append (entry.getKey ()).append ("=").append (entry.getValue ());
			if (count < parameters.size () - 1) {
				sb.append ("&");
			}
			count++;
		}
	}

	public static class MediaType {
		public final static String URL_ENCODED = "application/x-www-form-urlencoded";
		public final static String JSON = "application/json";
	}

	public static class Headers extends HashMap<String, List<String>> {
		public final static String CONTENT_TYPE = "Content-Type";
		public final static String ACCEPT = "Accept";

		private static Headers generator (Object... o) {
			Objects.requireNonNull (o);
			if (o.length % 2 != 0) {
				throw new IllegalArgumentException ();
			}
			Headers headers = new Headers ();
			if (!ObjectUtil.isEmpty (o)) {
				for (int i = 0, n = o.length; i < n; i += 2) {
					headers.put ((String) o[i], (List<String>) o[i + 1]);
				}
			}
			return headers;
		}
	}

	public static class Parameters extends HashMap<String, Object> {
		private static Parameters generator (Object... o) {
			Objects.requireNonNull (o);
			if (o.length % 2 != 0) {
				throw new IllegalArgumentException ();
			}
			Parameters parameters = new Parameters ();
			if (!ObjectUtil.isEmpty (o)) {
				for (int i = 0, n = o.length; i < n; i += 2) {
					parameters.put ((String) o[i], o[i + 1]);
				}
			}
			return parameters;
		}
	}

	public static class HttpClientBuilder {
		private String url;
		private Parameters parameters;
		private Headers headers;
		private String requestBody;

		public HttpClientBuilder url (String url) {
			Objects.requireNonNull (url);
			this.url = url;
			return this;
		}

		public HttpClientBuilder addParameter (String parameterName, Object value) {
			Objects.requireNonNull (parameterName);
			Objects.requireNonNull (value);
			if (parameters == null) {
				parameters = new Parameters ();
			}
			parameters.put (parameterName, value);
			return this;
		}

		public HttpClientBuilder addParameters (Object... o) {
			Objects.requireNonNull (o);
			if (parameters == null) {
				parameters = Parameters.generator (o);
			} else {
				if (o.length % 2 != 0) {
					throw new IllegalArgumentException ();
				}
				for (int i = 0; i < o.length; i += 2) {
					parameters.put ((String) o[i], o[i + 1]);
				}
			}
			return this;
		}

		public HttpClientBuilder setParameters (Map<String, Object> parameters) {
			Objects.requireNonNull (parameters);
			if (this.parameters == null) {
				this.parameters = new Parameters ();
			}
			this.parameters.putAll (parameters);
			return this;
		}

		public HttpClientBuilder addParametersFromObject (Object o) {
			Objects.requireNonNull (o);
			if (o instanceof String) {
				try {
					parameters = new ObjectMapper ().readValue ((String) o, Parameters.class);
				} catch (JsonProcessingException e) {
					throw new RuntimeException ("Failed to read parameters", e);
				}
			} else {
				parameters = new ObjectMapper ().convertValue (o, Parameters.class);
			}
			return this;
		}

		public HttpClientBuilder contentType (String contentType) {
			Objects.requireNonNull (contentType);
			if (headers == null) {
				headers = new Headers ();
			}
			headers.putIfAbsent (Headers.CONTENT_TYPE, List.of (contentType));
			return this;
		}

		public HttpClientBuilder accept (String... acceptableMediaTypes) {
			Objects.requireNonNull (acceptableMediaTypes);
			if (headers == null) {
				headers = new Headers ();
			}
			headers.putIfAbsent (Headers.ACCEPT, List.of (acceptableMediaTypes));
			return this;
		}

		public HttpClientBuilder addHeader (String headerName, String value) {
			Objects.requireNonNull (headerName);
			Objects.requireNonNull (value);
			if (headers == null) {
				headers = new Headers ();
			}
			headers.putIfAbsent (headerName, List.of (value));
			return this;
		}

		public HttpClientBuilder addHeaders (Object... o) {
			Objects.requireNonNull (o);
			if (headers == null) {
				headers = Headers.generator (o);
			} else {
				if (o.length % 2 != 0) {
					throw new IllegalArgumentException ();
				}
				for (int i = 0; i < o.length; i += 2) {
					headers.put ((String) o[i], (List<String>) o[i + 1]);
				}
			}
			return this;
		}

		public HttpClientBuilder setHeaders (Map<String, List<String>> headers) {
			Objects.requireNonNull (headers);
			if (this.headers == null) {
				this.headers = new Headers ();
			}
			this.headers.putAll (headers);
			return this;
		}

		public HttpClientBuilder requestBody (String requestBody) {
			Objects.requireNonNull (requestBody);
			this.requestBody = requestBody;
			return this;
		}

		private HttpClient build () {
			return new HttpClient (url, parameters, headers, requestBody);
		}

		public HttpClient get () {
			return build ().get ();
		}

		public HttpClient post () {
			return build ().post ();
		}

		public HttpClient put () {
			return build ().put ();
		}

		public HttpClient delete () {
			return build ().delete ();
		}

		public HttpClient patch () {
			return build ().patch ();
		}
	}
}
