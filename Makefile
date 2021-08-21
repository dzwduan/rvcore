TOP = TopMain
BUILD_DIR = ./build
TOP_V = $(BUILD_DIR)/$(TOP).v
SCALA_FILE = $(shell find ./src/main/scala -name '*.scala')

IMAGE = "inst/dummy-riscv64-mycpu.bin"

$(TOP_V): $(SCALA_FILE)
	mkdir -p $(@D)
	sbt 'runMain top.$(TOP) -td $(@D) --output-file $@'

test:
	sbt 'test:runMain rvcore.TestMain -td $(BUILD_DIR) --image $(IMAGE)'

emu:
	sbt 'test:runMain rvcore.TestMain -td $(BUILD_DIR) --image $(IMAGE) --backend-name verilator --generate-vcd-output on'


clean:
	rm -rf test_run_dir
