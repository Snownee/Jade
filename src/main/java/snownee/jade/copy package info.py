import os
import re
import shutil

package_pattern = re.compile(r"^package [\w\.]+;$", re.MULTILINE)

def copy_package_info():
    """
    读取当前目录的 package-info.java 文件，并在所有子孙目录中创建同名文件。
    """
    current_dir = os.getcwd()
    source_file_path = os.path.join(current_dir, "package-info.java")

    if not os.path.isfile(source_file_path):
        print(f"错误：当前目录中找不到文件 '{source_file_path}'。")
        return

    try:
        with open(source_file_path, 'r', encoding='utf-8') as source_file:
            content = source_file.read()
    except Exception as e:
        print(f"读取文件 '{source_file_path}' 时发生错误：{e}")
        return

    for root, dirs, files in os.walk(current_dir):
        # 跳过当前目录，因为我们已经读取了当前目录的文件
        if root == current_dir:
            continue

        # 如果当前目录没有java文件，则跳过
        if not any(file.endswith('.java') for file in files):
            continue

        # 如果当前目录只有package-info.java文件，则删除package-info.java文件
        if len(files) == 1 and files[0] == "package-info.java":
            package_info_path = os.path.join(root, "package-info.java")
            try:
                os.remove(package_info_path)
                print(f"已删除 '{package_info_path}' 文件")
            except Exception as e:
                print(f"删除文件 '{package_info_path}' 时发生错误：{e}")
            continue

        destination_file_path = os.path.join(root, "package-info.java")
        try:
            with open(destination_file_path, 'w', encoding='utf-8') as destination_file:
                destination_file.write(modify_content(content, root))
            print(f"已将 '{source_file_path}' 的内容写入到 '{destination_file_path}'")
        except Exception as e:
            print(f"写入文件 '{destination_file_path}' 时发生错误：{e}")

def modify_content(content, root):
    """
    修改内容以适应不同的目录结构。
    """
    # 提取包名
    match = package_pattern.search(content)
    if match:
        package_name = match.group(0)[8:-1]  # 去掉 "package " 和 ";"
        # 根据目录结构修改包名
        relative_path = os.path.relpath(root, os.getcwd())
        new_package_name = f"{package_name}.{relative_path.replace(os.sep, '.')}"
        content = content.replace(package_name, new_package_name, 1)
    return content

if __name__ == "__main__":
    copy_package_info()