/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.ml.naivebayes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.ml.model.AbstractModel;
import opennlp.tools.ml.model.AbstractModelWriter;
import opennlp.tools.ml.model.ComparablePredicate;
import opennlp.tools.ml.model.Context;

/**
 * The base class for {@link NaiveBayesModel} writers.
 * <p>
 * It provides the {@link #persist()} method which takes care of the structure
 * of a stored document, and requires an extending class to define precisely
 * how the data should be stored.
 *
 * @see NaiveBayesModel
 * @see AbstractModelWriter
 */
public abstract class NaiveBayesModelWriter extends AbstractModelWriter {

  private static final Logger logger = LoggerFactory.getLogger(NaiveBayesModelWriter.class);

  protected Context[] PARAMS;
  protected String[] OUTCOME_LABELS;
  protected String[] PRED_LABELS;
  int numOutcomes;

  public NaiveBayesModelWriter(AbstractModel model) {

    Object[] data = model.getDataStructures();
    this.numOutcomes = model.getNumOutcomes();
    PARAMS = (Context[]) data[0];

    @SuppressWarnings("unchecked")
    Map<String, Context> pmap = (Map<String, Context>) data[1];

    OUTCOME_LABELS = (String[]) data[2];
    PARAMS = new Context[pmap.size()];
    PRED_LABELS = new String[pmap.size()];

    int i = 0;
    for (Map.Entry<String, Context> pred : pmap.entrySet()) {
      PRED_LABELS[i] = pred.getKey();
      PARAMS[i] = pred.getValue();
      i++;
    }
  }

  /**
   * Sorts and optimizes the model parameters.
   *
   * @return A {@link ComparablePredicate[]}.
   */
  protected ComparablePredicate[] sortValues() {

    ComparablePredicate[] sortPreds = new ComparablePredicate[PARAMS.length];

    int numParams = 0;
    for (int pid = 0; pid < PARAMS.length; pid++) {
      int[] predkeys = PARAMS[pid].getOutcomes();
      // Arrays.sort(predkeys);
      int numActive = predkeys.length;
      double[] activeParams = PARAMS[pid].getParameters();

      numParams += numActive;
      /*
       * double[] activeParams = new double[numActive];
       *
       * int id = 0; for (int i=0; i < predkeys.length; i++) { int oid =
       * predkeys[i]; activeOutcomes[id] = oid; activeParams[id] =
       * PARAMS[pid].getParams(oid); id++; }
       */
      sortPreds[pid] = new ComparablePredicate(PRED_LABELS[pid],
          predkeys, activeParams);
    }

    Arrays.sort(sortPreds);
    return sortPreds;
  }

  /**
   * Compresses outcome patterns.
   *
   * @return A {@link List} of {@link List<ComparablePredicate>} that represent
   *         the remaining outcomes patterns.
   */
  protected List<List<ComparablePredicate>> compressOutcomes(ComparablePredicate[] sorted) {
    List<List<ComparablePredicate>> outcomePatterns = new ArrayList<>();
    if (sorted.length > 0) {
      ComparablePredicate cp = sorted[0];
      List<ComparablePredicate> newGroup = new ArrayList<>();
      for (int i = 0; i < sorted.length; i++) {
        if (cp.compareTo(sorted[i]) == 0) {
          newGroup.add(sorted[i]);
        } else {
          cp = sorted[i];
          outcomePatterns.add(newGroup);
          newGroup = new ArrayList<>();
          newGroup.add(sorted[i]);
        }
      }
      outcomePatterns.add(newGroup);
    }
    return outcomePatterns;
  }

  /**
   * Computes outcome patterns via {@link ComparablePredicate[] predicates}.
   *
   * @return A {@link List} of {@link List<ComparablePredicate>} that represent
   *         the outcomes patterns.
   */
  protected List<List<ComparablePredicate>> computeOutcomePatterns(ComparablePredicate[] sorted) {
    ComparablePredicate cp = sorted[0];
    List<List<ComparablePredicate>> outcomePatterns = new ArrayList<>();
    List<ComparablePredicate> newGroup = new ArrayList<>();
    for (ComparablePredicate predicate : sorted) {
      if (cp.compareTo(predicate) == 0) {
        newGroup.add(predicate);
      } else {
        cp = predicate;
        outcomePatterns.add(newGroup);
        newGroup = new ArrayList<>();
        newGroup.add(predicate);
      }
    }
    outcomePatterns.add(newGroup);
    logger.info("{} outcome patterns", outcomePatterns.size());
    return outcomePatterns;
  }
  
  /**
   * Writes the {@link AbstractModel perceptron model}, using the
   * {@link #writeUTF(String)}, {@link #writeDouble(double)}, or {@link #writeInt(int)}}
   * methods implemented by extending classes.
   *
   * <p>If you wish to create a {@link NaiveBayesModelWriter} which uses a different
   * structure, it will be necessary to override the {@code #persist()} method in
   * addition to implementing the {@code writeX(..)} methods.
   *
   * @throws IOException Thrown if IO errors occurred.
   */
  @Override
  public void persist() throws IOException {

    // the type of model (NaiveBayes)
    writeUTF("NaiveBayes");

    // the mapping from outcomes to their integer indexes
    writeInt(OUTCOME_LABELS.length);

    for (String label : OUTCOME_LABELS) {
      writeUTF(label);
    }

    // the mapping from predicates to the outcomes they contributed to.
    // The sorting is done so that we actually can write this out more
    // compactly than as the entire list.
    ComparablePredicate[] sorted = sortValues();
    List<List<ComparablePredicate>> compressed = computeOutcomePatterns(sorted);

    writeInt(compressed.size());

    for (List<ComparablePredicate> a : compressed) {
      writeUTF(a.size() + a.get(0).toString());
    }

    // the mapping from predicate names to their integer indexes
    writeInt(sorted.length);

    for (ComparablePredicate s : sorted) {
      writeUTF(s.name);
    }

    // write out the parameters
    for (int i = 0; i < sorted.length; i++)
      for (int j = 0; j < sorted[i].params.length; j++)
        writeDouble(sorted[i].params[j]);

    close();
  }
}
