#include <string.h>
#include <tree_sitter/api.h>

// Wrappers for native methods that allow passing structs by value (SN doesn't
// support that)
void ts_tree_root_node_ptr(TSTree *tree, TSNode *_return) {
  TSNode raw = ts_tree_root_node(tree);
  memcpy(_return, &raw, sizeof(TSNode));
}

char *ts_node_string_ptr(TSNode *node) { return ts_node_string(*node); }
bool ts_node_is_null_ptr(TSNode *node) { return ts_node_is_null(*node); };

uint32_t ts_node_child_count_ptr(TSNode *node) {
  return ts_node_child_count(*node);
}

const char *ts_node_type_ptr(TSNode *node) { return ts_node_type(*node); }

void ts_node_child_ptr(TSNode *node, uint32_t index, TSNode *_return) {
  TSNode raw = ts_node_child(*node, index);
  memcpy(_return, &raw, sizeof(TSNode));
}

uint32_t ts_node_start_byte_ptr(TSNode *node) {
  return ts_node_start_byte(*node);
}

uint32_t ts_node_end_byte_ptr(TSNode *node) { return ts_node_end_byte(*node); }

const char *ts_node_field_name_for_child_ptr(TSNode *node, uint32_t index) {
  return ts_node_field_name_for_child(*node, index);
}
