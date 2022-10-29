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
