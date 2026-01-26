import sys
import json
import tree_sitter_arkts as arkts
from tree_sitter import Language, Parser

def get_node_text(source_bytes, node):
    return source_bytes[node.start_byte:node.end_byte].decode('utf-8')

def find_child_by_type(node, type_name):
    for child in node.children:
        if child.type == type_name:
            return child
    return None

def parse_source(source_bytes):
    try:
        ARKTS_LANGUAGE = Language(arkts.language())
        parser = Parser(ARKTS_LANGUAGE)
        tree = parser.parse(source_bytes)
        
        slices = []
        cursor = tree.walk()
        
        visited_root = False
        while not visited_root:
            node = cursor.node
            node_type = node.type
            
            slice_type = None
            name = "anonymous"
            
            # Map node types to SliceType
            if node_type == 'component_declaration': 
                slice_type = 'STRUCT'
                # Try field 'name' or find identifier child
                name_node = node.child_by_field_name('name')
                if not name_node:
                    name_node = find_child_by_type(node, 'identifier')
                if name_node:
                    name = get_node_text(source_bytes, name_node)
                    
            elif node_type == 'struct_declaration':
                slice_type = 'STRUCT'
                name_node = node.child_by_field_name('name')
                if not name_node:
                    name_node = find_child_by_type(node, 'identifier')
                if name_node:
                    name = get_node_text(source_bytes, name_node)

            elif node_type == 'class_declaration':
                slice_type = 'CLASS'
                name_node = node.child_by_field_name('name')
                if not name_node:
                    name_node = find_child_by_type(node, 'identifier')
                if name_node:
                    name = get_node_text(source_bytes, name_node)

            elif node_type == 'interface_declaration':
                slice_type = 'INTERFACE'
                name_node = node.child_by_field_name('name')
                if not name_node:
                    name_node = find_child_by_type(node, 'identifier')
                if name_node:
                    name = get_node_text(source_bytes, name_node)

            elif node_type == 'function_declaration' or node_type == 'method_definition':
                slice_type = 'FUNCTION'
                name_node = node.child_by_field_name('name')
                if not name_node:
                    name_node = find_child_by_type(node, 'identifier')
                if name_node:
                    name = get_node_text(source_bytes, name_node)

            elif node_type == 'function_expression':
                # Check if it has a name (named function expression)
                name_node = node.child_by_field_name('name')
                if not name_node:
                    name_node = find_child_by_type(node, 'identifier')
                
                if name_node:
                    slice_type = 'FUNCTION'
                    name = get_node_text(source_bytes, name_node)

            elif node_type == 'enum_declaration':
                slice_type = 'ENUM'
                name_node = node.child_by_field_name('name')
                if not name_node:
                    name_node = find_child_by_type(node, 'identifier')
                if name_node:
                    name = get_node_text(source_bytes, name_node)
            
            if slice_type:
                start_line = node.start_point[0] + 1
                end_line = node.end_point[0] + 1
                content = get_node_text(source_bytes, node)
                
                slices.append({
                    "name": name,
                    "type": slice_type,
                    "content": content,
                    "startLine": start_line,
                    "endLine": end_line,
                    "language": "ARKTS"
                })

            # Traverse tree
            if cursor.goto_first_child():
                continue
            if cursor.goto_next_sibling():
                continue
            
            retracing = True
            while retracing:
                if not cursor.goto_parent():
                    retracing = False
                    visited_root = True
                else:
                    if cursor.goto_next_sibling():
                        retracing = False
                        
        return slices
    except Exception as e:
        return {"error": str(e)}

def parse_file(file_path):
    try:
        with open(file_path, 'rb') as f:
            source_bytes = f.read()
        return parse_source(source_bytes)
    except Exception as e:
        return {"error": str(e)}

def parse_content(content):
    try:
        return parse_source(content.encode('utf-8'))
    except Exception as e:
        return {"error": str(e)}

def run_daemon():
    while True:
        try:
            line = sys.stdin.readline()
            if not line:
                break
            
            # Support both JSON request and legacy raw file path
            result = []
            try:
                request = json.loads(line)
                if isinstance(request, dict):
                    if 'content' in request:
                        result = parse_content(request['content'])
                    elif 'file' in request:
                        result = parse_file(request['file'])
                    else:
                        result = {"error": "Invalid request: missing 'file' or 'content' field"}
                else:
                    # If line is a JSON string but not a dict, treat as file path if it looks like one?
                    # Safer to just fallback to file path logic if not a dict.
                    result = parse_file(line.strip())
            except json.JSONDecodeError:
                # Not JSON, treat as raw file path (backward compatibility)
                file_path = line.strip()
                if not file_path:
                    continue
                result = parse_file(file_path)
            
            print(json.dumps(result))
            sys.stdout.flush()
        except Exception as e:
            print(json.dumps({"error": str(e)}))
            sys.stdout.flush()

def main():
    if len(sys.argv) > 1 and sys.argv[1] == "--daemon":
        run_daemon()
        return

    if len(sys.argv) < 2:
        print(json.dumps({"error": "No file path provided"}))
        return

    file_path = sys.argv[1]
    result = parse_file(file_path)
    print(json.dumps(result))

if __name__ == "__main__":
    main()
