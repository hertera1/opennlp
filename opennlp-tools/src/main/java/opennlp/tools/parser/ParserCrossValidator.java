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

package opennlp.tools.parser;

import java.io.IOException;

import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.CrossValidationPartitioner;
import opennlp.tools.util.eval.FMeasure;

/**
 * Cross validator for a {@link Parser}.
 */
public class ParserCrossValidator {

  private final String languageCode;

  private final TrainingParameters params;

  private final HeadRules rules;

  private final FMeasure fmeasure = new FMeasure();

  private final ParserType parserType;

  private final ParserEvaluationMonitor[] monitors;

  /**
   * Initializes a {@link ParserCrossValidator} instance via given parameters.
   *
   * @param languageCode An ISO conform language code.
   * @param params The {@link TrainingParameters} for the context of cross validation.
   * @param rules The {@link HeadRules} for the context of cross validation.
   * @param parserType The {@link ParserType} for the context of cross validation.
   * @param monitors the {@link ParserEvaluationMonitor evaluation listeners}.
   */
  public ParserCrossValidator(String languageCode, TrainingParameters params,
      HeadRules rules, ParserType parserType, ParserEvaluationMonitor... monitors) {
    this.languageCode = languageCode;
    this.params = params;
    this.rules = rules;
    this.parserType = parserType;
    this.monitors = monitors;
  }

  /**
   * Starts the evaluation.
   *
   * @param samples The {@link ObjectStream} of {@link Parse samples} to train and test with.
   * @param nFolds Number of folds. It must be greater than zero.
   *
   * @throws IOException Thrown if IO errors occurred.
   * @throws IllegalStateException Thrown if the currently active {@link ParserType} is not supported.
   */
  public void evaluate(ObjectStream<Parse> samples, int nFolds) throws IOException {

    CrossValidationPartitioner<Parse> partitioner = new CrossValidationPartitioner<>(samples, nFolds);

    while (partitioner.hasNext()) {
      CrossValidationPartitioner.TrainingSampleStream<Parse> trainingSampleStream = partitioner.next();

      ParserModel model;
      if (ParserType.CHUNKING.equals(parserType)) {
        model = opennlp.tools.parser.chunking.Parser.train(languageCode, samples, rules, params);
      }
      else if (ParserType.TREEINSERT.equals(parserType)) {
        model = opennlp.tools.parser.treeinsert.Parser.train(languageCode, samples, rules, params);
      }
      else {
        throw new IllegalStateException("Unexpected parser type: " + parserType);
      }

      ParserEvaluator evaluator = new ParserEvaluator(ParserFactory.create(model), monitors);
      evaluator.evaluate(trainingSampleStream.getTestSampleStream());

      fmeasure.mergeInto(evaluator.getFMeasure());
    }
  }

  public FMeasure getFMeasure() {
    return fmeasure;
  }
}
