/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.edu.tsinghua.iginx.utils;

import cn.edu.tsinghua.iginx.thrift.DataType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ByteUtils {

	public static ByteBuffer getByteBuffer(Object value, DataType dataType) {
		ByteBuffer buffer;

		switch (dataType) {
			case BOOLEAN:
				buffer = ByteBuffer.allocate(1);
				buffer.put(booleanToByte((boolean) value));
				break;
			case INTEGER:
				buffer = ByteBuffer.allocate(4);
				buffer.putInt((int) value);
				break;
			case LONG:
				buffer = ByteBuffer.allocate(8);
				buffer.putLong((long) value);
				break;
			case FLOAT:
				buffer = ByteBuffer.allocate(4);
				buffer.putFloat((float) value);
				break;
			case DOUBLE:
				buffer = ByteBuffer.allocate(8);
				buffer.putDouble((double) value);
				break;
			case STRING:
				buffer = ByteBuffer.allocate(4 + ((byte[]) value).length);
				buffer.putInt(((byte[]) value).length);
				buffer.put((byte[]) value);
				break;
			default:
				throw new UnsupportedOperationException(dataType.toString());
		}

		buffer.flip();
		return buffer;
	}

	public static boolean[] getBooleanArray(ByteBuffer buffer) {
		boolean[] array = new boolean[buffer.array().length];
		for (int i = 0; i < array.length; i++) {
			array[i] = buffer.get() == 1;
		}
		return array;
	}

	public static String[] getStringArray(ByteBuffer buffer) {
		List<String> tempList = new ArrayList<>();
		int cnt = 0;
		while (cnt < buffer.array().length) {
			int length = buffer.getInt();
			byte[] bytes = new byte[length];
			buffer.get(bytes, 0, length);
			tempList.add(new String(bytes, 0, length));
			cnt += length + 4;
		}
		return tempList.toArray(new String[0]);
	}

	public static byte booleanToByte(boolean x) {
		if (x) {
			return 1;
		} else {
			return 0;
		}
	}

	public static Object[] getValuesListByDataType(List<ByteBuffer> valuesList, List<DataType> dataTypeList) {
		Object[] tempValues = new Object[valuesList.size()];
		for (int i = 0; i < valuesList.size(); i++) {
			switch (dataTypeList.get(i)) {
				case BOOLEAN:
					tempValues[i] = getBooleanArray(valuesList.get(i));
					break;
				case INTEGER:
					tempValues[i] = valuesList.get(i).asIntBuffer().array();
					break;
				case LONG:
					tempValues[i] = valuesList.get(i).asLongBuffer().array();
					break;
				case FLOAT:
					tempValues[i] = valuesList.get(i).asFloatBuffer().array();
					break;
				case DOUBLE:
					tempValues[i] = valuesList.get(i).asDoubleBuffer().array();
					break;
				case STRING:
					tempValues[i] = getStringArray(valuesList.get(i));
					break;
				default:
					throw new UnsupportedOperationException(dataTypeList.get(i).toString());
			}
		}
		return tempValues;
	}

	public static byte[] getByteArrayFromLongArray(long[] array) {
		ByteBuffer buffer = ByteBuffer.allocate(array.length * 8);
		buffer.asLongBuffer().put(array);
		return buffer.array();
	}

	public static List<ByteBuffer> getByteBufferByDataType(Object[] valuesList, List<DataType> dataTypeList) {
		List<ByteBuffer> byteBufferList = new ArrayList<>();
		for (int i = 0; i < valuesList.length; i++) {
			byteBufferList.add(getByteBuffer((Object[]) valuesList[i], dataTypeList.get(i)));
		}
		return byteBufferList;
	}

	public static ByteBuffer getByteBuffer(Object[] values, DataType dataType) {
		ByteBuffer buffer = ByteBuffer.allocate(getByteBufferSize(values, dataType));
		switch (dataType) {
			case BOOLEAN:
				for (Object value : values) {
					buffer.put(booleanToByte((boolean) value));
				}
				break;
			case INTEGER:
				for (Object value : values) {
					buffer.putInt((int) value);
				}
				break;
			case LONG:
				for (Object value : values) {
					buffer.putLong((long) value);
				}
				break;
			case FLOAT:
				for (Object value : values) {
					buffer.putFloat((float) value);
				}
				break;
			case DOUBLE:
				for (Object value : values) {
					buffer.putDouble((double) value);
				}
				break;
			case STRING:
				for (Object value : values) {
					buffer.putInt(((byte[]) value).length);
					buffer.put((byte[]) value);
				}
				break;
			default:
				throw new UnsupportedOperationException(dataType.toString());
		}
		buffer.flip();
		return buffer;
	}

	public static int getByteBufferSize(Object[] values, DataType dataType) {
		int size = 0;
		switch (dataType) {
			case BOOLEAN:
				size = values.length;
				break;
			case INTEGER:
			case FLOAT:
				size = values.length * 4;
				break;
			case LONG:
			case DOUBLE:
				size = values.length * 8;
				break;
			case STRING:
				size += values.length * 4;
				for (Object value : values) {
					size += ((byte[]) value).length;
				}
				break;
			default:
				throw new UnsupportedOperationException(dataType.toString());
		}
		return size;
	}
}
