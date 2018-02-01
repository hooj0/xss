package com.masget.xss.wrapper;

import java.util.*;
import java.io.*;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.FileItem;

/**
 * Wrapper for a file upload request (before Servlet 3.0).
 * <P>
 * This class uses the Apache Commons <a href='http://commons.apache.org/fileupload/'>File Upload tool</a>. The generous
 * Apache License will very likely allow you to use it in your applications as well.
 */
public class FileUploadWrapper extends HttpServletRequestWrapper {

	/** Constructor. */
	public FileUploadWrapper(HttpServletRequest aRequest) throws IOException {
		super(aRequest);
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		try {
			List<FileItem> fileItems = upload.parseRequest(aRequest);
			convertToMaps(fileItems);
		} catch (FileUploadException ex) {
			throw new IOException("Cannot parse underlying request: " + ex.toString());
		}
	}

	/**
	 * Return all request parameter names, for both regular controls and file upload controls.
	 */
	@Override
	public Enumeration<String> getParameterNames() {
		Set<String> allNames = new LinkedHashSet<>();
		allNames.addAll(regularParams.keySet());
		allNames.addAll(fileParams.keySet());
		return Collections.enumeration(allNames);
	}

	/**
	 * Return the parameter value. Applies only to regular parameters, not to file upload parameters.
	 * <P>
	 * If the parameter is not present in the underlying request, then <tt>null</tt> is returned.
	 * <P>
	 * If the parameter is present, but has no associated value, then an empty string is returned.
	 * <P>
	 * If the parameter is multivalued, return the first value that appears in the request.
	 */
	@Override
	public String getParameter(String aName) {
		String result = null;
		List<String> values = regularParams.get(aName);
		if (values == null) {
			// you might try the wrappee, to see if it has a value
		} else if (values.isEmpty()) {
			// param name known, but no values present
			result = "";
		} else {
			// return first value in list
			result = values.get(FIRST_VALUE);
		}
		return result;
	}

	/**
	 * Return the parameter values. Applies only to regular parameters, not to file upload parameters.
	 */
	@Override
	public String[] getParameterValues(String aName) {
		String[] result = null;
		List<String> values = regularParams.get(aName);
		if (values != null) {
			result = values.toArray(new String[values.size()]);
		}
		return result;
	}

	/**
	 * Return a {@code Map<String, List<String>>} for all regular parameters. Does not return any file upload parameters
	 * at all.
	 */
	@Override
	public Map<String, String[]> getParameterMap() {
		return Maps.transformValues(regularParams, new Function<List<String>, String[]>() {

			@Override
			public String[] apply(List<String> input) {
				String[] s = new String[] {};
				return input.toArray(s);
			}

		});
	}

	/**
	 * Return a {@code List<FileItem>}, in the same order as they appear in the underlying request.
	 */
	public List<FileItem> getFileItems() {
		return new ArrayList<FileItem>(fileParams.values());
	}

	/**
	 * Return the {@link FileItem} of the given name.
	 * <P>
	 * If the name is unknown, then return <tt>null</tt>.
	 */
	public FileItem getFileItem(String aFieldName) {
		return fileParams.get(aFieldName);
	}

	// PRIVATE

	/** Store regular params only. May be multivalued (hence the List). */
	private final Map<String, List<String>> regularParams = new LinkedHashMap<>();

	/** Store file params only. */
	private final Map<String, FileItem> fileParams = new LinkedHashMap<>();
	private static final int FIRST_VALUE = 0;

	private static final String charset = "utf-8";

	private void convertToMaps(List<FileItem> fileItems) throws UnsupportedEncodingException {
		for (FileItem item : fileItems) {
			if (isFileUploadField(item)) {
				fileParams.put(item.getFieldName(), item);
			} else {
				if (alreadyHasValue(item)) {
					addMultivaluedItem(item);
				} else {
					addSingleValueItem(item);
				}
			}
		}
	}

	private boolean isFileUploadField(FileItem fileItem) {
		return !fileItem.isFormField();
	}

	private boolean alreadyHasValue(FileItem item) {
		return regularParams.get(item.getFieldName()) != null;
	}

	private void addSingleValueItem(FileItem item) throws UnsupportedEncodingException {
		List<String> list = new ArrayList<>();
		list.add(item.getString(charset));
		regularParams.put(item.getFieldName(), list);
	}

	private void addMultivaluedItem(FileItem item) throws UnsupportedEncodingException {
		List<String> values = regularParams.get(item.getFieldName());
		values.add(item.getString(charset));
	}
}
