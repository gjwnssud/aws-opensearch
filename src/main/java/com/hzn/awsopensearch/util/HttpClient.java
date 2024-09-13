package com.hzn.awsopensearch.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzn.awsopensearch.dto.Response;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@SuppressWarnings ({"unchecked"})
@RequiredArgsConstructor
public class HttpClient {
	private final String url;
	private final Parameters parameters;
	private final Headers headers;
	@Getter
	private Response<String> response;

	public static HttpClientBuilder builder () {
		return new HttpClientBuilder ();
	}

	private HttpClient get () {
		response = request (url, HttpMethod.GET.name (), headers, parameters);
		return this;
	}

	private HttpClient post () {
		response = request (url, HttpMethod.POST.name (), headers, parameters);
		return this;
	}

	private HttpClient put () {
		response = request (url, HttpMethod.PUT.name (), headers, parameters);
		return this;
	}

	private HttpClient patch () {
		response = request (url, HttpMethod.PATCH.name (), headers, parameters);
		return this;
	}

	private HttpClient delete () {
		response = request (url, HttpMethod.DELETE.name (), headers, parameters);
		return this;
	}

	public Response<Map<String, Object>> getResponseByMap () {
		try {
			Map<String, Object> responseBodyMap = new ObjectMapper ().readValue (response.getData (), Map.class);
			return Response.of (response.getCode (), response.getMessage (), responseBodyMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException ("Failed to parse response body", e);
		}
	}

	public <T> Response<T> getResponseByClass (Class<T> clazz) {
		try {
			T cls = new ObjectMapper ().readValue (response.getData (), clazz);
			return Response.of (response.getCode (), response.getMessage (), cls);
		} catch (JsonProcessingException e) {
			throw new RuntimeException ("Failed to parse response body", e);
		}
	}

	private Response<String> request (String spec, String method, Headers headers, Parameters parameters) {
		StringBuilder urlSb = new StringBuilder (spec);
		if (HttpMethod.GET.name ().equalsIgnoreCase (method) && !ObjectUtils.isEmpty (parameters)) {
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

		if (ObjectUtils.isEmpty (contentType.get ())) {
			contentType.set (MediaType.URL_ENCODED);
			connection.setRequestProperty ("Content-Type", MediaType.JSON);
		}

		if (HttpMethod.POST.name ().equalsIgnoreCase (method) || HttpMethod.PUT.name ().equalsIgnoreCase (method) || HttpMethod.PATCH.name ().equalsIgnoreCase (method)) {
			connection.setDoOutput (true);
			try (OutputStream os = connection.getOutputStream ()) {
				if (contentType.get ().contains (MediaType.JSON)) {
					os.write (new ObjectMapper ().writeValueAsString (parameters).getBytes (StandardCharsets.UTF_8));
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

		return Response.of (code, HttpStatus.valueOf (code).getMessage (), sb.toString ());
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
			if (!ObjectUtils.isEmpty (o)) {
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
			if (!ObjectUtils.isEmpty (o)) {
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

		public HttpClientBuilder addParametersFromObject (Object o) {
			Objects.requireNonNull (o);
			parameters = new ObjectMapper ().convertValue (o, Parameters.class);
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

		private HttpClient build () {
			return new HttpClient (url, parameters, headers);
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

	public enum HttpMethod {
		GET,
		POST,
		PUT,
		DELETE,
		HEAD,
		OPTIONS,
		PATCH,
		TRACE,
		CONNECT;
	}

	@Getter
	@RequiredArgsConstructor
	public enum HttpStatus {
		// Informational responses
		CONTINUE (100, "Continue"),
		SWITCHING_PROTOCOLS (101, "Switching Protocols"),
		PROCESSING (102, "Processing"),
		EARLY_HINTS (103, "Early Hints"),

		// Successful responses
		OK (200, "OK"),
		CREATED (201, "Created"),
		ACCEPTED (202, "Accepted"),
		NON_AUTHORITATIVE_INFORMATION (203, "Non-Authoritative Information"),
		NO_CONTENT (204, "No Content"),
		RESET_CONTENT (205, "Reset Content"),
		PARTIAL_CONTENT (206, "Partial Content"),
		MULTI_STATUS (207, "Multi-Status"),
		ALREADY_REPORTED (208, "Already Reported"),
		IM_USED (226, "IM Used"),

		// Redirection messages
		MULTIPLE_CHOICES (300, "Multiple Choices"),
		MOVED_PERMANENTLY (301, "Moved Permanently"),
		FOUND (302, "Found"),
		SEE_OTHER (303, "See Other"),
		NOT_MODIFIED (304, "Not Modified"),
		USE_PROXY (305, "Use Proxy"),
		TEMPORARY_REDIRECT (307, "Temporary Redirect"),
		PERMANENT_REDIRECT (308, "Permanent Redirect"),

		// Client error responses
		BAD_REQUEST (400, "Bad Request"),
		UNAUTHORIZED (401, "Unauthorized"),
		PAYMENT_REQUIRED (402, "Payment Required"),
		FORBIDDEN (403, "Forbidden"),
		NOT_FOUND (404, "Not Found"),
		METHOD_NOT_ALLOWED (405, "Method Not Allowed"),
		NOT_ACCEPTABLE (406, "Not Acceptable"),
		PROXY_AUTHENTICATION_REQUIRED (407, "Proxy Authentication Required"),
		REQUEST_TIMEOUT (408, "Request Timeout"),
		CONFLICT (409, "Conflict"),
		GONE (410, "Gone"),
		LENGTH_REQUIRED (411, "Length Required"),
		PRECONDITION_FAILED (412, "Precondition Failed"),
		PAYLOAD_TOO_LARGE (413, "Payload Too Large"),
		URI_TOO_LONG (414, "URI Too Long"),
		UNSUPPORTED_MEDIA_TYPE (415, "Unsupported Media Type"),
		RANGE_NOT_SATISFIABLE (416, "Range Not Satisfiable"),
		EXPECTATION_FAILED (417, "Expectation Failed"),
		IM_A_TEAPOT (418, "I'm a teapot"),
		MISDIRECTED_REQUEST (421, "Misdirected Request"),
		UNPROCESSABLE_ENTITY (422, "Unprocessable Entity"),
		LOCKED (423, "Locked"),
		FAILED_DEPENDENCY (424, "Failed Dependency"),
		TOO_EARLY (425, "Too Early"),
		UPGRADE_REQUIRED (426, "Upgrade Required"),
		PRECONDITION_REQUIRED (428, "Precondition Required"),
		TOO_MANY_REQUESTS (429, "Too Many Requests"),
		REQUEST_HEADER_FIELDS_TOO_LARGE (431, "Request Header Fields Too Large"),
		UNAVAILABLE_FOR_LEGAL_REASONS (451, "Unavailable For Legal Reasons"),

		// Server error responses
		INTERNAL_SERVER_ERROR (500, "Internal Server Error"),
		NOT_IMPLEMENTED (501, "Not Implemented"),
		BAD_GATEWAY (502, "Bad Gateway"),
		SERVICE_UNAVAILABLE (503, "Service Unavailable"),
		GATEWAY_TIMEOUT (504, "Gateway Timeout"),
		HTTP_VERSION_NOT_SUPPORTED (505, "HTTP Version Not Supported"),
		VARIANT_ALSO_NEGOTIATES (506, "Variant Also Negotiates"),
		INSUFFICIENT_STORAGE (507, "Insufficient Storage"),
		LOOP_DETECTED (508, "Loop Detected"),
		NOT_EXTENDED (510, "Not Extended"),
		NETWORK_AUTHENTICATION_REQUIRED (511, "Network Authentication Required");

		private final int code;
		private final String message;
		private final static HttpStatus[] VALUES = values ();

		public static HttpStatus valueOf (int code) {
			return Arrays.stream (VALUES)
			             .filter (hs -> hs.getCode () == code)
			             .findFirst ()
			             .orElseThrow (() -> new IllegalArgumentException ("Unknown HTTP status code: " + code));
		}
	}
}
