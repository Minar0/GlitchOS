package com.whmin.zapsos.userIntents
//
//import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
//import edu.stanford.nlp.pipeline.StanfordCoreNLP
//import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation
//import java.util.*
//
//
//public class IntentRecogManager {
//    var pipeline: StanfordCoreNLP? = null
//    fun init() {
//        val props = Properties()
//        props.setProperty("annotators", "tokenize, ssplit, pos, parse")
//        pipeline = StanfordCoreNLP(props)
//    }
//
//    fun findIntent(text: String?) {
//        val annotation = pipeline!!.process(text)
//        for (sentence in annotation.get(SentencesAnnotation::class.java)) {
//            val sg = sentence.get(BasicDependenciesAnnotation::class.java)
//            var intent = "It does not seem that the sentence expresses an explicit intent."
//            for (edge in sg.edgeIterable()) {
//                if (edge.relation.longName === "direct object") {
//                    val tverb = edge.governor.originalText()
//                    var dobj = edge.dependent.originalText()
//                    dobj = dobj.substring(0, 1).uppercase(Locale.getDefault()) + dobj.substring(1)
//                        .lowercase(
//                            Locale.getDefault()
//                        )
//                    intent = tverb + dobj
//                }
//            }
//            println("Sentence:\t$sentence")
//            println("Intent:\t\t$intent\n")
//        }
//    }
//}