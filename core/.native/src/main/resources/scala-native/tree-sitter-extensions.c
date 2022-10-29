#include <string.h>
#include <tree_sitter/api.h>

// Wrappers for native methods that allow passing structs by value (SN doesn't
// support that)

// magic incantations to workaround scala-native#2778
// many thanks to @armanbilge for the hint
// disabled for now
// #pragma weak ts_tree_root_node
// #pragma weak ts_node_string
// #pragma weak ts_node_is_null

void ts_tree_root_node_ptr(TSTree *tree, TSNode *_return) {
  TSNode raw = ts_tree_root_node(tree);
  memcpy(_return, &raw, sizeof(TSNode));
}

char *ts_node_string_ptr(TSNode *node) { return ts_node_string(*node); }
bool ts_node_is_null_ptr(TSNode *node) { return ts_node_is_null(*node); };
