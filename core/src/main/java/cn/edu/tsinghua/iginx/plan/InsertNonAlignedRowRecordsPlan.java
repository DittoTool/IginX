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
package cn.edu.tsinghua.iginx.plan;

import cn.edu.tsinghua.iginx.metadata.entity.StorageUnitMeta;
import cn.edu.tsinghua.iginx.metadata.entity.TimeSeriesInterval;
import cn.edu.tsinghua.iginx.thrift.DataType;
import cn.edu.tsinghua.iginx.utils.Bitmap;
import cn.edu.tsinghua.iginx.utils.Pair;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ToString
public class InsertNonAlignedRowRecordsPlan extends InsertRowRecordsPlan {

    private static final Logger logger = LoggerFactory.getLogger(InsertNonAlignedRowRecordsPlan.class);

    private List<Bitmap> bitmapList;

    public InsertNonAlignedRowRecordsPlan(List<String> paths, long[] timestamps, Object[] valuesList, List<Bitmap> bitmapList,
                                          List<DataType> dataTypeList, List<Map<String, String>> attributesList, StorageUnitMeta storageUnit) {
        super(paths, timestamps, valuesList, dataTypeList, attributesList, storageUnit);
        this.bitmapList = bitmapList;
        this.setIginxPlanType(IginxPlanType.INSERT_NON_ALIGNED_ROW_RECORDS);
    }

    public InsertNonAlignedRowRecordsPlan(List<String> paths, long[] timestamps, Object[] valuesList, List<Bitmap> bitmapList,
                                          List<DataType> dataTypeList, List<Map<String, String>> attributesList) {
        this(paths, timestamps, valuesList, bitmapList, dataTypeList, attributesList, null);
    }


    public Bitmap getBitmap(int index) {
        if (bitmapList == null || bitmapList.isEmpty()) {
            logger.error("There are no bitmaps in the InsertRecordsPlan.");
            return null;
        }
        if (index < 0 || index >= bitmapList.size()) {
            logger.error("The given index {} is out of bounds.", index);
            return null;
        }
        return bitmapList.get(index);
    }

    public Pair<Object[], List<Bitmap>> getValuesAndBitmapsByIndexes(Pair<Integer, Integer> rowIndexes, TimeSeriesInterval interval) {
        if (getValuesList() == null || getValuesList().length == 0) {
            logger.error("There are no values in the InsertNonAlignedRowRecordsPlan.");
            return null;
        }
        int startIndex;
        int endIndex;
        if (interval.getStartTimeSeries() == null) {
            startIndex = 0;
        } else {
            startIndex = getPathsNum();
            for (int i = 0; i < getPathsNum(); i++) {
                if (getPath(i).compareTo(interval.getStartTimeSeries()) >= 0) {
                    startIndex = i;
                    break;
                }
            }
        }
        if (interval.getEndTimeSeries() == null) {
            endIndex = getPathsNum() - 1;
        } else {
            endIndex = -1;
            for (int i = getPathsNum() - 1; i >= 0; i--) {
                if (getPath(i).compareTo(interval.getEndTimeSeries()) <= 0) {
                    endIndex = i;
                    break;
                }
            }
        }

        Object[] tempValues = new Object[rowIndexes.v - rowIndexes.k + 1];
        List<Bitmap> tempBitmaps = new ArrayList<>();
        for (int i = rowIndexes.k; i <= rowIndexes.v; i++) {
            Bitmap tempBitmap = new Bitmap(endIndex - startIndex + 1);
            List<Integer> indexes = new ArrayList<>();
            int k = 0;
            for (int j = 0; j < getPathsNum(); j++) {
                if (getBitmap(i).get(j)) {
                    if (j >= startIndex && j <= endIndex) {
                        indexes.add(k);
                        tempBitmap.mark(j - startIndex);
                    }
                    k++;
                }
            }
            Object[] tempRowValues = new Object[indexes.size()];
            for (int j = 0; j < indexes.size(); j++) {
                tempRowValues[j] = getValues(i)[indexes.get(j)];
            }
            tempValues[i - rowIndexes.k] = tempRowValues;
            tempBitmaps.add(tempBitmap);
        }
        return new Pair<>(tempValues, tempBitmaps);
    }

}
