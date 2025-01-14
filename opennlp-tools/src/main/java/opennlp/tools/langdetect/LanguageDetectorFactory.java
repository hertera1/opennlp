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

package opennlp.tools.langdetect;

import opennlp.tools.util.BaseToolFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ext.ExtensionLoader;
import opennlp.tools.util.normalizer.EmojiCharSequenceNormalizer;
import opennlp.tools.util.normalizer.NumberCharSequenceNormalizer;
import opennlp.tools.util.normalizer.ShrinkCharSequenceNormalizer;
import opennlp.tools.util.normalizer.TwitterCharSequenceNormalizer;
import opennlp.tools.util.normalizer.UrlCharSequenceNormalizer;


/**
 * Default factory used by {@link LanguageDetector}.
 *
 * Extend this class to change the Language Detector behaviour,
 * such as the {@link LanguageDetectorContextGenerator}.
 * The default {@link DefaultLanguageDetectorContextGenerator} will use char n-grams of
 * size 1 to 3 and the following normalizers:
 * <ul>
 * <li> {@link EmojiCharSequenceNormalizer}
 * <li> {@link UrlCharSequenceNormalizer}
 * <li> {@link TwitterCharSequenceNormalizer}
 * <li> {@link NumberCharSequenceNormalizer}
 * <li> {@link ShrinkCharSequenceNormalizer}
 * </ul>
 */
public class LanguageDetectorFactory extends BaseToolFactory {

  /**
   * @return Retrieves a {@link LanguageDetectorContextGenerator}.
   */
  public LanguageDetectorContextGenerator getContextGenerator() {
    return new DefaultLanguageDetectorContextGenerator(1, 3,
        EmojiCharSequenceNormalizer.getInstance(),
        UrlCharSequenceNormalizer.getInstance(),
        TwitterCharSequenceNormalizer.getInstance(),
        NumberCharSequenceNormalizer.getInstance(),
        ShrinkCharSequenceNormalizer.getInstance());
  }

  /**
   * Instantiates a {@link LanguageDetectorFactory} via a given {@code subclassName}.
   *
   * @param subclassName The class name used for instantiation. If {@code null}, an
   *                     instance of {@link LanguageDetectorFactory} will be returned
   *                     per default. Otherwise, the {@link ExtensionLoader} mechanism
   *                     is applied to load the requested {@code subclassName}.
   *                     
   * @return A valid {@link LanguageDetectorFactory} instance.
   * @throws InvalidFormatException Thrown if the {@link ExtensionLoader} mechanism failed to
   *                                create the factory associated with {@code subclassName}.
   */
  public static LanguageDetectorFactory create(String subclassName)
      throws InvalidFormatException {
    if (subclassName == null) {
      // will create the default factory
      return new LanguageDetectorFactory();
    }
    try {
      LanguageDetectorFactory theFactory = ExtensionLoader.instantiateExtension(
          LanguageDetectorFactory.class, subclassName);
      theFactory.init();
      return theFactory;
    } catch (Exception e) {
      String msg = "Could not instantiate the " + subclassName
          + ". The initialization threw an exception.";
      throw new InvalidFormatException(msg, e);
    }
  }

  public void init() {
    // nothing to do
  }

  @Override
  public void validateArtifactMap() throws InvalidFormatException {
    // nothing to validate
  }
}
