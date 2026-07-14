# Top-level Makefile that dispatches to each Java example project.
#
# Targets:
#   install  - one-time setup: prime the local ~/.m2 repo, apply Spotless.
#              Ordered so that library artifacts (ner_dbpedia,
#              semantic_web_apache_jena) are installed before the projects
#              that consume them (kgc, kgn).
#   check    - fast offline compile check across every subproject.
#   test     - run every subproject's test target.
#   clean    - clean every subproject's build output.
#   fmt      - Spotless-format every subproject.
#   fmt-check - verify formatting across every subproject.
#   verify   - fmt-check + check across every subproject.

# Alphabetical iteration order for check/test/clean/fmt/fmt-check/verify.
SUBDIRS := \
	AgentScope_gemini \
	anomaly_detection \
	gemini-llm-client \
	genetic-algorithms \
	info_gathering \
	kgc \
	kgn \
	langchain4j-ollama \
	ner_dbpedia \
	neuralnetworks \
	nlp \
	ollama-llm-client \
	openai-llm-client \
	search \
	semantic_web_apache_jena

# Dependency-ordered list for `install`: leaf libraries first, then the
# projects that depend on them.
INSTALL_ORDER := \
	AgentScope_gemini \
	anomaly_detection \
	gemini-llm-client \
	genetic-algorithms \
	info_gathering \
	langchain4j-ollama \
	neuralnetworks \
	nlp \
	ollama-llm-client \
	openai-llm-client \
	search \
	ner_dbpedia \
	semantic_web_apache_jena \
	kgc \
	kgn

.PHONY: check test clean fmt fmt-check verify install

install:
	@for dir in $(INSTALL_ORDER); do \
		echo "==> $$dir : install"; \
		$(MAKE) -C $$dir install || exit $$?; \
	done

check test clean fmt fmt-check verify:
	@for dir in $(SUBDIRS); do \
		echo "==> $$dir : $@"; \
		$(MAKE) -C $$dir $@ || exit $$?; \
	done
