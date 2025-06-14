JAVAC=javac
SRC_DIRS=ap25unit1/ap25 ap25unit1/myplayer
BIN_DIR=bin
N?=100
DEPTH?=2
SOURCES=$(foreach dir,$(SRC_DIRS),$(wildcard $(dir)/*.java))

test: all
	@for classfile in $(CLASSES); do \
        classname=$$(echo $$classfile | sed 's|$(BIN_DIR)/||;s|/|.|g;s|\.class$$||'); \
        java -cp $(BIN_DIR) $$classname; \
	done

.PHONY: all clean

all:
	mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) $(SOURCES)

clean:
	rm -rf $(BIN_DIR)

1c:
	$(MAKE) all
	# 先手の${N}回
	@for i in $(shell seq 1 $(N)); do \
	    java -cp $(BIN_DIR) myplayer.MyGame former ${DEPTH}; \
		printf "▇"; \
	done; \
	printf "\n"; \
	java -cp $(BIN_DIR) myplayer.ResultAnalysis
	# 後手の${N}回
	@for i in $(shell seq 1 $(N)); do \
	    java -cp $(BIN_DIR) myplayer.MyGame latter ${DEPTH}; \
		printf "▇"; \
	done; \
	printf "\n"; \
	java -cp $(BIN_DIR) myplayer.ResultAnalysis