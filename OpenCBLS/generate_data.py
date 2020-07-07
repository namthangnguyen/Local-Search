import random

random.seed(5)


def gen_data(days, time_slots, num_rooms, num_classes, num_teachers):
    # Kich thuoc phong ngau nhien trong 3 so 50, 100 va 200
    list_room_cap = [50, 100, 200]*int(num_rooms/3)
    while len(list_room_cap) < num_rooms:
        list_room_cap.append(50)
    random.shuffle(list_room_cap)
    # So hoc sinh trong moi lop hoc ngau nhien trong 3 so 50, 100, 200
    list_class_student = [50, 100, 200]*int(num_classes/3)
    while len(list_class_student) < num_classes:
        list_class_student.append(100)
    random.shuffle(list_class_student)
    # So tiet hoc cua moi lop hoc ngau nhien trong 3 so 2, 3, 4
    list_class_unit = [2, 3, 4]*int(num_classes/3)
    while len(list_class_unit) < num_classes:
        list_class_unit.append(4)
    random.shuffle(list_class_unit)
    # Giao vien se duoc phan vao ngau nhien cac lop,
    # moi giao vien se day so luong lop bang nhau tru giao vien dau tien
    list_class_teacher = list(range(num_teachers)) * \
        int(num_classes/num_teachers)
    while len(list_class_teacher) < num_classes:
        list_class_teacher.append(0)
    random.shuffle(list_class_teacher)

    data_path = './data/' + str(num_classes) + '_' + \
        str(num_rooms) + '_' + str(num_teachers) + '.txt'
    with open(data_path, 'w') as f:
        f.write(str(days))
        f.write("\n" + str(time_slots))
        f.write("\n" + str(num_rooms))
        f.write("\n" + str(num_classes))
        f.write("\n" + str(num_teachers))
        f.write("\n")
        f.write(str(list_room_cap[0]))
        for rc in list_room_cap[1:]:
            f.write(" " + str(rc))
        f.write("\n")
        f.write(str(list_class_student[0]))
        for cs in list_class_student[1:]:
            f.write(" " + str(cs))
        f.write("\n")
        f.write(str(list_class_unit[0]))
        for cu in list_class_unit[1:]:
            f.write(" " + str(cu))
        f.write("\n")
        f.write(str(list_class_teacher[0]))
        for ct in list_class_teacher[1:]:
            f.write(" " + str(ct))
        # f.write("\n")


def main():
    days = 5
    time_slots = 12
    num_rooms = 3
    num_classes = 17
    num_teachers = 5
    gen_data(days, time_slots, num_rooms, num_classes, num_teachers)


if __name__ == '__main__':
    main()
