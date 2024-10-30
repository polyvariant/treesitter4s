#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>

// POC of 'reflection' on tree-sitter grammar dylibs in C
// to see if we can apply the same paradigm on all scala platforms
// and avoid language implementors having to write any binding traits whatsoever.
// usage: cc fun-times.c -o get-lang && ./get-lang python
// (requires the dylib to be in the current dir of course)
int main(int argc, char *argv[])
{
  // Ensure a language name is provided
  if (argc != 2)
  {
    fprintf(stderr, "Usage: %s <language_name>\n", argv[0]);
    return 1;
  }

  const char *language_name = argv[1]; // Read the language name from argv

  // Construct the shared library name dynamically
  char lib_name[256];
  snprintf(lib_name, sizeof(lib_name), "libtree-sitter-%s.dylib", language_name);

  // Load the shared library
  void *handle = dlopen(lib_name, RTLD_LAZY);
  if (!handle)
  {
    fprintf(stderr, "Error loading library: %s\n", dlerror());
    return 1;
  }

  const char *func_name_prefix = "tree_sitter"; // Prefix for the function name
  char func_name_full[256];

  // Construct the full function name dynamically
  snprintf(func_name_full, sizeof(func_name_full), "%s_%s", func_name_prefix, language_name);

  void *(*func)(); // Adjust the function pointer type to return a pointer

  // Use dlsym to get the function address
  *(void **)(&func) = dlsym(handle, func_name_full);

  // Check for errors in retrieving the function
  char *error = dlerror();
  if (error != NULL)
  {
    fprintf(stderr, "Error locating function: %s\n", error);
    dlclose(handle);
    return 1;
  }

  // Call the function and get the returned pointer
  void *result = func();

  // Print the address of the returned pointer
  printf("Address of the returned pointer: %p\n", result);

  // Clean up
  dlclose(handle);
  return 0;
}
