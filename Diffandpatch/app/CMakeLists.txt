

cmake_minimum_required(VERSION 3.4.1)



#添加多个目录的思路 指定一个变量 添加的时候使用变量值(my_c_path)
file(GLOB my_c_path src/main/cpp/*.c)

add_library( # Sets the name of the library.
             patch-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${my_c_path}
             )



find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )



target_link_libraries( # Specifies the target library.
                       patch-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )