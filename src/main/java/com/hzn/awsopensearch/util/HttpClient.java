package com.hzn.awsopensearch.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzn.awsopensearch.dto.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
public class HttpClient {
	private final static String GET = HttpMethod.GET.name ();
	private final static String POST = HttpMethod.POST.name ();
	private final static String PUT = HttpMethod.PUT.name ();
	private final static String PATCH = HttpMethod.PATCH.name ();

	private final static ObjectMapper objectMapper = new ObjectMapper ();

	public static Response<String> request (String spec) throws IOException {
		return request (spec, GET, null, null, null);
	}

	public static Response<String> request (String spec, Headers headers) throws IOException {
		return request (spec, GET, headers, null, null);
	}

	public static Response<String> request (String spec, Headers headers, Parameters parameters) throws IOException {
		return request (spec, GET, headers, parameters, null);
	}

	public static Response<String> request (String spec, String method, Object object) throws IOException {
		return request (spec, method, null, null, object);
	}

	public static Response<String> request (String spec, String method, Headers headers) throws IOException {
		return request (spec, method, headers, null, null);
	}

	public static Response<String> request (String spec, String method, Headers headers, Object object) throws IOException {
		return request (spec, method, headers, null, object);
	}

	public static Response<String> request (String spec, String method, Headers headers, Parameters parameters, Object object) throws IOException {
		StringBuilder urlSb = new StringBuilder (spec);
		if (GET.equalsIgnoreCase (method) && !ObjectUtils.isEmpty (parameters)) {
			queryString (urlSb, parameters, method);
		}
		URL url = new URL (urlSb.toString ());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection ();
		connection.setRequestMethod (method);

		AtomicReference<String> contentType = new AtomicReference<> ();
		if (headers != null) {
			headers.forEach ((k, v) -> {
				String values = String.join ("", v);
				if (k.equals (HttpHeaders.CONTENT_TYPE)) {
					contentType.set (values);
				}
				connection.setRequestProperty (k, values);
			});
		}

		if (ObjectUtils.isEmpty (contentType.get ())) {
			contentType.set (MediaType.APPLICATION_FORM_URLENCODED_VALUE);
			connection.setRequestProperty ("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		}

		if (POST.equalsIgnoreCase (method) || PUT.equalsIgnoreCase (method) || PATCH.equalsIgnoreCase (method)) {
			connection.setDoOutput (true);
			OutputStream os = connection.getOutputStream ();
			if (contentType.get ().contains (MediaType.APPLICATION_JSON_VALUE)) {
				if (!ObjectUtils.isEmpty (parameters)) {
					os.write (objectMapper.writeValueAsString (parameters).getBytes (StandardCharsets.UTF_8));
				} else {
					if (object instanceof String) {
						os.write (((String) object).getBytes (StandardCharsets.UTF_8));
					} else {
						os.write (objectMapper.writeValueAsString (object).getBytes (StandardCharsets.UTF_8));
					}
				}
			} else {
				StringBuilder sb = new StringBuilder ();
				Map<String, Object> paramMap;
				if (!ObjectUtils.isEmpty (parameters)) {
					paramMap = parameters;
				} else {
					paramMap = objectMapper.convertValue (object, Map.class);
				}
				queryString (sb, paramMap, method);
				os.write (sb.toString ().getBytes (StandardCharsets.UTF_8));
			}
		}

		int code = connection.getResponseCode ();
		StringBuilder sb = new StringBuilder ();
		BufferedReader bis;
		if (code == HttpStatus.OK.value ()) {
			bis = new BufferedReader (new InputStreamReader (connection.getInputStream ()));
		} else {
			bis = new BufferedReader (new InputStreamReader (connection.getErrorStream ()));
		}
		String line;
		while ((line = bis.readLine ()) != null) {
			sb.append (line);
		}
		return Response.of (code, HttpStatus.valueOf (code).getReasonPhrase (), sb.toString ());
	}

	private static void queryString (StringBuilder sb, Map<String, Object> parameters, String method) {
		if (GET.equals (method)) {
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

	@Getter
	public static class Headers extends HttpHeaders {
		private final Map<String, String> customHeaders = new HashMap<> ();

		@Builder
		private Headers (MediaType contentType, List<MediaType> accept, Map<String, String> customHeaders) {
			if (!ObjectUtils.isEmpty (contentType)) {
				super.setContentType (contentType);
			}
			if (!ObjectUtils.isEmpty (accept)) {
				super.setAccept (accept);
			}
			if (!ObjectUtils.isEmpty (customHeaders)) {
				this.customHeaders.putAll (customHeaders);
			}

			this.customHeaders.forEach (super::set);
		}
	}

	public static class Parameters extends HashMap<String, Object> {
		public static Parameters of () {
			return generator ();
		}

		public static Parameters of (String k1, Object v1) {
			return generator (k1, v1);
		}

		public static Parameters of (String k1, Object v1, String k2, Object v2) {
			return generator (k1, v1, k2, v2);
		}

		public static Parameters of (String k1, Object v1, String k2, Object v2, String k3, Object v3) {
			return generator (k1, v1, k2, v2, k3, v3);
		}

		public static Parameters of (String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4) {
			return generator (k1, v1, k2, v2, k3, v3, k4, v4);
		}

		private static Parameters generator (Object... object) {
			Parameters parameters = new Parameters ();
			if (!ObjectUtils.isEmpty (object)) {
				for (int i = 0, n = object.length; i < n; i += 2) {
					parameters.put ((String) object[i], object[i + 1]);
				}
			}
			return parameters;
		}
	}
}
